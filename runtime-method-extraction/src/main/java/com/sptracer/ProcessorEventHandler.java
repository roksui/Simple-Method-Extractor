package com.sptracer;

import com.lmax.disruptor.EventHandler;
import com.sptracer.configuration.ConfigurationRegistry;
import com.sptracer.util.DependencyInjectingServiceLoader;

import java.util.List;

public class ProcessorEventHandler implements EventHandler<ReportingEvent> {

    private final List<Processor> processors;

    private ProcessorEventHandler(List<Processor> processors) {
        this.processors = processors;
    }

    public static ProcessorEventHandler loadProcessors(ConfigurationRegistry configurationRegistry) {
        return new ProcessorEventHandler(DependencyInjectingServiceLoader.load(Processor.class, configurationRegistry));
    }

    @Override
    public void onEvent(ReportingEvent event, long sequence, boolean endOfBatch) {
        if (event.getTransaction() != null) {
            for (int i = 0; i < processors.size(); i++) {
                processors.get(i).processBeforeReport(event.getTransaction());
            }
        } else if (event.getError() != null) {
            for (int i = 0; i < processors.size(); i++) {
                processors.get(i).processBeforeReport(event.getError());
            }
        }
    }
}
