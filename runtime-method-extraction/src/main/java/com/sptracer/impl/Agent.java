package com.sptracer.impl;

import java.util.UUID;

/**
 * Name and version of the Elastic APM agent
 */
public class Agent {

    /**
     * Name of the Elastic APM agent, e.g. "Python"
     * (Required)
     */
    private final String name;
    /**
     * Version of the Elastic APM agent, e.g."1.0.0"
     * (Required)
     */
    private final String version;

    /**
     * A unique agent ID, non-persistent (i.e. changes on restart).
     * <a href="https://www.elastic.co/guide/en/ecs/master/ecs-agent.html#_agent_field_details">See ECS for reference</a>.
     */
    private final String ephemeralId;

    public Agent(String name, String version) {
        this(name, version, UUID.randomUUID().toString());
    }

    public Agent(String name, String version, String ephemeralId) {
        this.name = name;
        this.version = version;
        this.ephemeralId = ephemeralId;
    }

    /**
     * Name of the Elastic APM agent, e.g. "Python"
     * (Required)
     */
    public String getName() {
        return name;
    }

    /**
     * Version of the Elastic APM agent, e.g."1.0.0"
     * (Required)
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return A unique agent ID, non-persistent (i.e. changes on restart).
     */
    public String getEphemeralId() {
        return ephemeralId;
    }
}
