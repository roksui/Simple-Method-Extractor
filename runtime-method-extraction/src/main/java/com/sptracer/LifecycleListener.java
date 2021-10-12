package com.sptracer;

import com.sptracer.impl.SpTracerImpl;

public interface LifecycleListener {

    void init(SpTracerImpl tracer) throws Exception;


    void start(SpTracerImpl tracer) throws Exception;


    void pause() throws Exception;


    void resume() throws Exception;

    void stop() throws Exception;

}
