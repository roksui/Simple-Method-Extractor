package com.sptracer;


import com.sptracer.configuration.ConfigurationRegistry;
import com.sptracer.impl.MetaData;
import com.sptracer.impl.StacktraceConfiguration;

import javax.annotation.Nonnull;
import java.util.concurrent.Future;

public class ReporterFactory {

    public Reporter createReporter(ConfigurationRegistry configurationRegistry,
                                   TracerServerClient apmServerClient,
                                   Future<MetaData> metaData) {

        ReporterConfiguration reporterConfiguration = configurationRegistry.getConfig(ReporterConfiguration.class);
        ReportingEventHandler reportingEventHandler = getReportingEventHandler(configurationRegistry, reporterConfiguration, metaData, apmServerClient);
        return new TracerServerReporter(true, reporterConfiguration, reportingEventHandler);
    }

    @Nonnull
    private ReportingEventHandler getReportingEventHandler(ConfigurationRegistry configurationRegistry,
                                                           ReporterConfiguration reporterConfiguration,
                                                           Future<MetaData> metaData,
                                                           TracerServerClient apmServerClient) {

        DslJsonSerializer payloadSerializer = new DslJsonSerializer(configurationRegistry.getConfig(StacktraceConfiguration.class), apmServerClient, metaData);
        ProcessorEventHandler processorEventHandler = ProcessorEventHandler.loadProcessors(configurationRegistry);
        return new IntakeV2ReportingEventHandler(reporterConfiguration, processorEventHandler, payloadSerializer, apmServerClient);
    }

}