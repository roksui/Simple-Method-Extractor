package com.sptracer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;


public class AgentAttacher2 {
    // intentionally not static so that we can initLogging first
    private final Logger logger = LoggerFactory.getLogger(AgentAttacher.class);
    private final Arguments arguments;
    // intentionally not storing JvmInfo as it may hold potentially sensitive information (JVM args)
    // reduces the risk of exposing them in heap dumps
    private final Set<String> alreadySeenJvmPids = new HashSet<>();
    private final JvmDiscoverer jvmDiscoverer;

}
