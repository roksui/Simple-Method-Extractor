package com.sptracer;
import com.lmax.disruptor.EventHandler;

public interface ReportingEventHandler extends EventHandler<ReportingEvent> {

    void init(TracerServerReporter reporter);

    long getReported();

    long getDropped();

    void close();
}
