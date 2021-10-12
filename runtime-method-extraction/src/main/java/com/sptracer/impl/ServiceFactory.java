package com.sptracer.impl;

import com.sptracer.configuration.CoreConfiguration;
import com.sptracer.util.VersionUtils;

public class ServiceFactory {

    public Service createService(CoreConfiguration coreConfiguration, String ephemeralId) {
        return new Service()
                .withName(coreConfiguration.getServiceName())
                .withVersion(coreConfiguration.getServiceVersion())
                .withEnvironment(coreConfiguration.getEnvironment())
                .withAgent(new Agent("java", getAgentVersion(), ephemeralId))
                .withRuntime(new RuntimeInfo("Java", System.getProperty("java.version")))
                .withLanguage(new Language("Java", System.getProperty("java.version")))
                .withNode(new Node(coreConfiguration.getServiceNodeName()));
    }

    private String getAgentVersion() {
        String version = VersionUtils.getAgentVersion();
        if (version == null) {
            return "unknown";
        }
        return version;
    }
}
