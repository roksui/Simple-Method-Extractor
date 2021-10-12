package com.sptracer;

import com.sptracer.impl.GlobalTracer;

public abstract class TracerAwareInstrumentation extends SpTracerInstrumentation {

    public static final Tracer tracer = GlobalTracer.get();

}
