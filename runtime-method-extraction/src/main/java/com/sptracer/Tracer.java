package com.sptracer;

import com.sptracer.error.ErrorCapture;
import com.sptracer.impl.AbstractSpan;
import com.sptracer.impl.BinaryHeaderGetter;
import com.sptracer.impl.Sampler;
import com.sptracer.impl.TextHeaderGetter;

import javax.annotation.Nullable;

public interface Tracer {

    /**
     * Starts a trace-root transaction
     *
     * @param initiatingClassLoader the class loader corresponding to the service which initiated the creation of the transaction.
     *                              Used to determine the service name.
     * @return a transaction that will be the root of the current trace if the agent is currently RUNNING; null otherwise
     */
    @Nullable
    com.sptracer.impl.Transaction startRootTransaction(@Nullable ClassLoader initiatingClassLoader);

    @Nullable
    com.sptracer.impl.Transaction startRootTransaction(@Nullable ClassLoader initiatingClassLoader, long epochMicro);

    /**
     * Starts a trace-root transaction with a specified sampler and start timestamp
     *
     * @param sampler               the {@link Sampler} instance which is responsible for determining the sampling decision if this is a root transaction
     * @param epochMicros           the start timestamp
     * @param initiatingClassLoader the class loader corresponding to the service which initiated the creation of the transaction.
     *                              Used to determine the service name and to load application-scoped classes like the {@link org.slf4j.MDC},
     *                              for log correlation.
     * @return a transaction that will be the root of the current trace if the agent is currently RUNNING; null otherwise
     */
    @Nullable
    com.sptracer.impl.Transaction startRootTransaction(Sampler sampler, long epochMicros, @Nullable ClassLoader initiatingClassLoader);

    /**
     * Starts a transaction as a child of the context headers obtained through the provided {@link HeaderGetter}.
     * If the created transaction cannot be started as a child transaction (for example - if no parent context header is
     * available), then it will be started as the root transaction of the trace.
     *
     * @param headerCarrier         the Object from which context headers can be obtained, typically a request or a message
     * @param textHeadersGetter     provides the trace context headers required in order to create a child transaction
     * @param initiatingClassLoader the class loader corresponding to the service which initiated the creation of the transaction.
     *                              Used to determine the service name.
     * @return a transaction which is a child of the provided parent if the agent is currently RUNNING; null otherwise
     */
    @Nullable
    <C> com.sptracer.impl.Transaction startChildTransaction(@Nullable C headerCarrier, TextHeaderGetter<C> textHeadersGetter, @Nullable ClassLoader initiatingClassLoader);

    @Nullable
    <C> com.sptracer.impl.Transaction startChildTransaction(@Nullable C headerCarrier, TextHeaderGetter<C> textHeadersGetter, @Nullable ClassLoader initiatingClassLoader, long epochMicros);

    /**
     * Starts a transaction as a child of the context headers obtained through the provided {@link HeaderGetter}.
     * If the created transaction cannot be started as a child transaction (for example - if no parent context header is
     * available), then it will be started as the root transaction of the trace.
     *
     * @param headerCarrier         the Object from which context headers can be obtained, typically a request or a message
     * @param textHeadersGetter     provides the trace context headers required in order to create a child transaction
     * @param sampler               the {@link Sampler} instance which is responsible for determining the sampling decision if this is a root transaction
     * @param epochMicros           the start timestamp
     * @param initiatingClassLoader the class loader corresponding to the service which initiated the creation of the transaction.
     *                              Used to determine the service name and to load application-scoped classes like the {@link org.slf4j.MDC},
     *                              for log correlation.
     * @return a transaction which is a child of the provided parent if the agent is currently RUNNING; null otherwise
     */
    @Nullable
    <C> com.sptracer.impl.Transaction startChildTransaction(@Nullable C headerCarrier, TextHeaderGetter<C> textHeadersGetter, Sampler sampler,
                                          long epochMicros, @Nullable ClassLoader initiatingClassLoader);

    /**
     * Starts a transaction as a child of the context headers obtained through the provided {@link HeaderGetter}.
     * If the created transaction cannot be started as a child transaction (for example - if no parent context header is
     * available), then it will be started as the root transaction of the trace.
     *
     * @param headerCarrier         the Object from which context headers can be obtained, typically a request or a message
     * @param binaryHeadersGetter   provides the trace context headers required in order to create a child transaction
     * @param initiatingClassLoader the class loader corresponding to the service which initiated the creation of the transaction.
     *                              Used to determine the service name.
     * @return a transaction which is a child of the provided parent if the agent is currently RUNNING; null otherwise
     */
    @Nullable
    <C> com.sptracer.impl.Transaction startChildTransaction(@Nullable C headerCarrier, BinaryHeaderGetter<C> binaryHeadersGetter, @Nullable ClassLoader initiatingClassLoader);

    /**
     * Starts a transaction as a child of the context headers obtained through the provided {@link HeaderGetter}.
     * If the created transaction cannot be started as a child transaction (for example - if no parent context header is
     * available), then it will be started as the root transaction of the trace.
     *
     * @param headerCarrier         the Object from which context headers can be obtained, typically a request or a message
     * @param binaryHeadersGetter   provides the trace context headers required in order to create a child transaction
     * @param sampler               the {@link Sampler} instance which is responsible for determining the sampling decision if this is a root transaction
     * @param epochMicros           the start timestamp
     * @param initiatingClassLoader the class loader corresponding to the service which initiated the creation of the transaction.
     *                              Used to determine the service name and to load application-scoped classes like the {@link org.slf4j.MDC},
     *                              for log correlation.
     * @return a transaction which is a child of the provided parent if the agent is currently RUNNING; null otherwise
     */
    @Nullable
    <C> com.sptracer.impl.Transaction startChildTransaction(@Nullable C headerCarrier, BinaryHeaderGetter<C> binaryHeadersGetter,
                                          Sampler sampler, long epochMicros, @Nullable ClassLoader initiatingClassLoader);

    @Nullable
    com.sptracer.impl.Transaction currentTransaction();

    @Nullable
    AbstractSpan<?> getActive();

    @Nullable
    Span getActiveSpan();

    /**
     * Captures an exception without providing an explicit reference to a parent {@link AbstractSpan}
     *
     * @param e                     the exception to capture
     * @param initiatingClassLoader the class
     */
    void captureAndReportException(@Nullable Throwable e, ClassLoader initiatingClassLoader);

    @Nullable
    String captureAndReportException(long epochMicros, @Nullable Throwable e, @Nullable AbstractSpan<?> parent);

    @Nullable
    ErrorCapture captureException(@Nullable Throwable e, @Nullable AbstractSpan<?> parent, @Nullable ClassLoader initiatingClassLoader);

    @Nullable
    Span getActiveExitSpan();

    TracerState getState();

    /**
     * Overrides the service name for all {@link Transaction}s,
     * {@link Span}s and {@link ErrorCapture}s which are created by the service which corresponds to the provided {@link ClassLoader}.
     * <p>
     * The main use case is being able to differentiate between multiple services deployed to the same application server.
     * </p>
     *
     * @param classLoader the class loader which corresponds to a particular service
     * @param serviceName the service name for this class loader
     */
    void overrideServiceNameForClassLoader(@Nullable ClassLoader classLoader, @Nullable String serviceName);

    /**
     * Called when the container shuts down.
     * Cleans up thread pools and other resources.
     */
    void stop();

    boolean isRunning();

    @Nullable
    Span createExitChildSpan();

    /**
     * An enumeration used to represent the current tracer state.
     */
    enum TracerState {
        /**
         * The agent's state before it has been started for the first time.
         */
        UNINITIALIZED,

        /**
         * Indicates that the agent is currently fully functional - tracing, monitoring and sending data to the APM server.
         */
        RUNNING,

        /**
         * The agent is mostly idle, consuming minimal resources, ready to quickly resume back to RUNNING. When the agent
         * is PAUSED, it is not tracing and not communicating with the APM server. However, classes are still instrumented
         * and threads are still alive.
         */
        PAUSED,

        /**
         * Indicates that the agent had been stopped.
         * NOTE: this state is irreversible- the agent cannot resume if it has already been stopped.
         */
        STOPPED
    }
}
