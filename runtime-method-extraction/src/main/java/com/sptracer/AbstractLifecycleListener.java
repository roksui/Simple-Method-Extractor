package com.sptracer;

import com.sptracer.impl.SpTracerImpl;

public abstract class AbstractLifecycleListener implements LifecycleListener {
    @Override
    public void init(SpTracerImpl tracer) throws Exception {
    }

    @Override
    public void start(SpTracerImpl tracer) throws Exception {
    }

    @Override
    public void pause() throws Exception {
    }

    @Override
    public void resume() throws Exception {
    }

    @Override
    public void stop() throws Exception {
    }
}
