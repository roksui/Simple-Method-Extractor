package com.sptracer.error;

import com.sptracer.Recyclable;
import com.sptracer.TraceContext;
import com.sptracer.Transaction;
import com.sptracer.configuration.CoreConfiguration;
import com.sptracer.impl.*;
import com.sptracer.matcher.WildcardMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;


/**
 * Data captured by an agent representing an event occurring in a monitored service
 */
public class ErrorCapture implements Recyclable {

    private static final Logger logger = LoggerFactory.getLogger(ErrorCapture.class);

    private final TraceContext traceContext;

    /**
     * Context
     * <p>
     * Any arbitrary contextual information regarding the event, captured by the agent, optionally provided by the user
     */
    private final TransactionContext context = new TransactionContext();
    private final SpTracerImpl tracer;
    /**
     * Information about the originally thrown error.
     */
    @Nullable
    private Throwable exception;
    /**
     * Recorded time of the error, UTC based and formatted as YYYY-MM-DDTHH:mm:ss.sssZ
     * (Required)
     */
    private long timestamp;

    /**
     * Provides info about the Transaction corresponding this error
     */
    private TransactionInfo transactionInfo = new TransactionInfo();

    private final StringBuilder culprit = new StringBuilder();

    public ErrorCapture(SpTracerImpl tracer) {
        this.tracer = tracer;
        traceContext = TraceContext.with128BitId(this.tracer);
    }

    /**
     * Context
     * <p>
     * Any arbitrary contextual information regarding the event, captured by the agent, optionally provided by the user
     */
    public TransactionContext getContext() {
        return context;
    }

    /**
     * Information about the originally thrown error.
     */
    @Nullable
    public Throwable getException() {
        return exception;
    }

    /**
     * Recorded time of the error, UTC based and formatted as YYYY-MM-DDTHH:mm:ss.sssZ
     * (Required)
     */
    public long getTimestamp() {
        return timestamp;
    }

    public ErrorCapture withTimestamp(long epochMs) {
        this.timestamp = epochMs;
        return this;
    }

    @Override
    public void resetState() {
        exception = null;
        context.resetState();
        timestamp = 0;
        transactionInfo.resetState();
        traceContext.resetState();
        culprit.setLength(0);
    }

    public void recycle() {
        tracer.recycle(this);
    }

    /**
     * Creates a reference to a {@link TraceContext}
     *
     * @param parent parent trace context
     * @return {@code this}, for chaining
     */
    public ErrorCapture asChildOf(AbstractSpan<?> parent) {
        this.traceContext.asChildOf(parent.getTraceContext());
        if (traceContext.getTraceId().isEmpty()) {
            logger.debug("Creating an Error as child of {} with a null trace_id", parent.getNameAsString());
            if (logger.isTraceEnabled()) {
                logger.trace("Stack trace related to Error capture: ", new Throwable());
            }
        }
        if (parent instanceof Transaction) {
            Transaction transaction = (Transaction) parent;
            // The error might have occurred in a different thread than the one the transaction was recorded
            // That's why we have to ensure the visibility of the transaction properties
            context.copyFrom(transaction.getContextEnsureVisibility());
        } else if (parent instanceof Span) {
            Span span = (Span) parent;
            // TODO copy into SpanContext
            //  https://github.com/elastic/apm-agent-java/issues/279
            context.copyFrom(span.getContext());
        }
        return this;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }

    public void setException(Throwable e) {
        if (WildcardMatcher.anyMatch(tracer.getConfig(CoreConfiguration.class).getUnnestExceptions(), e.getClass().getName()) != null) {
            this.exception = e.getCause();
        } else {
            this.exception = e;
        }
    }

    public StringBuilder getCulprit() {
        // lazily resolve culprit so that java.lang.Throwable.getStackTrace is called outside the application thread
        final Collection<String> applicationPackages = tracer.getConfig(StacktraceConfiguration.class).getApplicationPackages();
        if (exception != null && culprit.length() == 0 && !applicationPackages.isEmpty()) {
            computeCulprit(exception, applicationPackages);
        }
        return culprit;
    }

    private void computeCulprit(Throwable exception, Collection<String> applicationPackages) {
        if (exception.getCause() != null) {
            computeCulprit(exception.getCause(), applicationPackages);
        }
        if (culprit.length() > 0) {
            return;
        }
        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
            for (String applicationPackage : applicationPackages) {
                if (stackTraceElement.getClassName().startsWith(applicationPackage)) {
                    setCulprit(stackTraceElement);
                    return;
                }
            }
        }
    }

    private void setCulprit(StackTraceElement stackTraceElement) {
        final int lineNumber = stackTraceElement.getLineNumber();
        final String fileName = stackTraceElement.getFileName();
        culprit.append(stackTraceElement.getClassName())
                .append('.')
                .append(stackTraceElement.getMethodName())
                .append('(');
        if (stackTraceElement.isNativeMethod()) {
            culprit.append("Native Method");
        } else {
            culprit.append(fileName != null ? fileName : "Unknown Source");
            if (lineNumber > 0) {
                culprit.append(':').append(lineNumber);
            }
        }
        culprit.append(')');
    }

    public ErrorCapture activate() {
        List<ActivationListener> activationListeners = tracer.getActivationListeners();
        for (int i = 0; i < activationListeners.size(); i++) {
            try {
                activationListeners.get(i).beforeActivate(this);
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                logger.warn("Exception while calling {}#beforeActivate", activationListeners.get(i).getClass().getSimpleName(), t);
            }
        }
        return this;
    }

    public ErrorCapture deactivate() {
        List<ActivationListener> activationListeners = tracer.getActivationListeners();
        for (int i = 0; i < activationListeners.size(); i++) {
            try {
                // `this` is guaranteed to not be recycled yet as the reference count is only decremented after this method has executed
                activationListeners.get(i).afterDeactivate(this);
            } catch (Error e) {
                throw e;
            } catch (Throwable t) {
                logger.warn("Exception while calling {}#afterDeactivate", activationListeners.get(i).getClass().getSimpleName(), t);
            }
        }
        return this;
    }

    public static class TransactionInfo implements Recyclable {
        /**
         * A hint for UI to be able to show whether a recorded trace for the corresponding transaction is expected
         */
        private boolean isSampled;
        /**
         * The related TransactionInfo type
         */
        @Nullable
        private String type;

        @Override
        public void resetState() {
            isSampled = false;
            type = null;
        }

        public boolean isSampled() {
            return isSampled;
        }

        @Nullable
        public String getType() {
            return type;
        }
    }

    public TransactionInfo getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionSampled(boolean transactionSampled) {
        transactionInfo.isSampled = transactionSampled;
    }

    public void setTransactionType(@Nullable String type) {
        transactionInfo.type = type;
    }

    public void end() {
        tracer.endError(this);
    }
}
