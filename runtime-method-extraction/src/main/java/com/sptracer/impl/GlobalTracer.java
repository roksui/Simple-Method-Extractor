package com.sptracer.impl;

import com.sptracer.Tracer;
import com.sptracer.error.ErrorCapture;
import com.sptracer.util.VersionUtils;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Objects;

public class GlobalTracer implements Tracer {

    private static final GlobalTracer INSTANCE = new GlobalTracer();
    private volatile Tracer tracer = NoopTracer.INSTANCE;
    private static volatile boolean classloaderCheckOk = false;

    private GlobalTracer() {
    }

    static {
        checkClassloader();
    }

    public static Tracer get() {
        return INSTANCE;
    }

    @Nullable
    public static SpTracerImpl getTracerImpl() {
        Tracer tracer = INSTANCE.tracer;
        if (tracer instanceof SpTracerImpl) {
            return ((SpTracerImpl) tracer);
        }
        return null;
    }

    public static SpTracerImpl requireTracerImpl() {
        return Objects.requireNonNull(getTracerImpl(), "Registered tracer is not an instance of ElasticApmTracer");
    }

    private static void checkClassloader() {
        ClassLoader cl = GlobalTracer.class.getClassLoader();

        // agent currently loaded in the bootstrap CL, which is the current correct location
        if (cl == null) {
            return;
        }

        if (classloaderCheckOk) {
            return;
        }

        String agentLocation = GlobalTracer.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        if (!agentLocation.endsWith(".jar")) {
            // agent is not packaged, thus we assume running tests
            classloaderCheckOk = true;
            return;
        }

        String premainClass = VersionUtils.getManifestEntry(new File(agentLocation), "Premain-Class");
        if (null == premainClass) {
            // packaged within a .jar, but not within an agent jar, thus we assume it's still for testing
            classloaderCheckOk = true;
            return;
        }

        if (premainClass.startsWith("co.elastic.apm.agent")) {
            // premain class will only be present when packaged as an agent jar
            classloaderCheckOk = true;
            return;
        }

        // A packaged agent class has been loaded outside of bootstrap classloader, we are not in the context of
        // unit/integration tests, that's likely a setup issue where the agent jar has been added to application
        // classpath.
        throw new IllegalStateException(String.format("Agent setup error: agent jar file \"%s\"  likely referenced in JVM or application classpath", agentLocation));

    }

    public static synchronized void setNoop() {
        TracerState currentTracerState = INSTANCE.tracer.getState();
        if (currentTracerState != TracerState.UNINITIALIZED && currentTracerState != TracerState.STOPPED) {
            throw new IllegalStateException("Can't override tracer as current tracer is already running");
        }
        INSTANCE.tracer = NoopTracer.INSTANCE;
    }

    public static synchronized void init(Tracer tracer) {
        if (!isNoop()) {
            throw new IllegalStateException("Tracer is already initialized");
        }
        INSTANCE.tracer = tracer;
    }

    public static boolean isNoop() {
        return INSTANCE.tracer == NoopTracer.INSTANCE;
    }

    @Nullable
    public Transaction startRootTransaction(@Nullable ClassLoader initiatingClassLoader) {
        return tracer.startRootTransaction(initiatingClassLoader);
    }

    @Nullable
    @Override
    public Transaction startRootTransaction(@Nullable ClassLoader initiatingClassLoader, long epochMicro) {
        return tracer.startRootTransaction(initiatingClassLoader, epochMicro);
    }

    @Nullable
    public Transaction startRootTransaction(Sampler sampler, long epochMicros, @Nullable ClassLoader initiatingClassLoader) {
        return tracer.startRootTransaction(sampler, epochMicros, initiatingClassLoader);
    }

    @Nullable
    public <C> Transaction startChildTransaction(@Nullable C headerCarrier, TextHeaderGetter<C> textHeadersGetter, @Nullable ClassLoader initiatingClassLoader) {
        return tracer.startChildTransaction(headerCarrier, textHeadersGetter, initiatingClassLoader);
    }

    @Nullable
    @Override
    public <C> Transaction startChildTransaction(@Nullable C headerCarrier, TextHeaderGetter<C> textHeadersGetter, @Nullable ClassLoader initiatingClassLoader, long epochMicros) {
        return tracer.startChildTransaction(headerCarrier, textHeadersGetter, initiatingClassLoader, epochMicros);
    }

    @Nullable
    public <C> Transaction startChildTransaction(@Nullable C headerCarrier, TextHeaderGetter<C> textHeadersGetter, Sampler sampler, long epochMicros, @Nullable ClassLoader initiatingClassLoader) {
        return tracer.startChildTransaction(headerCarrier, textHeadersGetter, sampler, epochMicros, initiatingClassLoader);
    }

    @Nullable
    public <C> Transaction startChildTransaction(@Nullable C headerCarrier, BinaryHeaderGetter<C> binaryHeadersGetter, @Nullable ClassLoader initiatingClassLoader) {
        return tracer.startChildTransaction(headerCarrier, binaryHeadersGetter, initiatingClassLoader);
    }

    @Nullable
    public <C> Transaction startChildTransaction(@Nullable C headerCarrier, BinaryHeaderGetter<C> binaryHeadersGetter, Sampler sampler, long epochMicros, @Nullable ClassLoader initiatingClassLoader) {
        return tracer.startChildTransaction(headerCarrier, binaryHeadersGetter, sampler, epochMicros, initiatingClassLoader);
    }

    @Nullable
    public Transaction currentTransaction() {
        return tracer.currentTransaction();
    }

    @Nullable
    @Override
    public AbstractSpan<?> getActive() {
        return tracer.getActive();
    }

    @Nullable
    @Override
    public Span getActiveSpan() {
        return tracer.getActiveSpan();
    }

    public void captureAndReportException(@Nullable Throwable e, ClassLoader initiatingClassLoader) {
        tracer.captureAndReportException(e, initiatingClassLoader);
    }

    @Nullable
    public String captureAndReportException(long epochMicros, @Nullable Throwable e, @Nullable AbstractSpan<?> parent) {
        return tracer.captureAndReportException(epochMicros, e, parent);
    }

    @Nullable
    public ErrorCapture captureException(@Nullable Throwable e, @Nullable AbstractSpan<?> parent, @Nullable ClassLoader initiatingClassLoader) {
        return tracer.captureException(e, parent, initiatingClassLoader);
    }

    @Nullable
    @Override
    public Span getActiveExitSpan() {
        return tracer.getActiveExitSpan();
    }

    @Override
    public Tracer.TracerState getState() {
        return tracer.getState();
    }

    @Override
    public void overrideServiceNameForClassLoader(@Nullable ClassLoader classLoader, @Nullable String serviceName) {
        tracer.overrideServiceNameForClassLoader(classLoader, serviceName);
    }

    @Override
    public void stop() {
        tracer.stop();
    }

    @Override
    public boolean isRunning() {
        return tracer.isRunning();
    }

    @Nullable
    @Override
    public Span createExitChildSpan() {
        return tracer.createExitChildSpan();
    }
}
