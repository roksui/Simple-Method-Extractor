package com.sptracer;

public final class Tracer {

    private static final ThreadLocal<CallStackElement> methodCallParent = new ThreadLocal<>();

    private Tracer() {
    }

    public static void start(String signature) {
        final CallStackElement parent = methodCallParent.get();
        if (parent != null) {
            methodCallParent.set(CallStackElement.create(parent, signature));
        }
    }

    public static void stop() {
        final CallStackElement currentElement = methodCallParent.get();
        if (currentElement != null) {
            methodCallParent.set(currentElement.executionStopped());
        }
    }

}
