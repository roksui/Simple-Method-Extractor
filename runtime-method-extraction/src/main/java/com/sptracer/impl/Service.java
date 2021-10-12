package com.sptracer.impl;

import javax.annotation.Nullable;


/**
 * Information about the instrumented Service
 */
public class Service {

    /**
     * Name and version of the Elastic APM agent
     * (Required)
     */
    @Nullable
    private Agent agent;
    /**
     * Name and version of the web framework used
     */
    @Nullable
    private Framework framework;
    /**
     * Name and version of the programming language used
     */
    @Nullable
    private Language language;
    /**
     * Representation of a service node
     */
    @Nullable
    private Node node;
    /**
     * Immutable name of the service emitting this event
     * (Required)
     */
    @Nullable
    private String name;
    /**
     * Environment name of the service, e.g. "production" or "staging"
     */
    @Nullable
    private String environment;
    /**
     * Name and version of the language runtime running this service
     */
    @Nullable
    private RuntimeInfo runtime;
    /**
     * Version of the service emitting this event
     */
    @Nullable
    private String version;

    /**
     * Name and version of the Elastic APM agent
     * (Required)
     */
    @Nullable
    public Agent getAgent() {
        return agent;
    }

    /**
     * Name and version of the Elastic APM agent
     * (Required)
     */
    public Service withAgent(Agent agent) {
        this.agent = agent;
        return this;
    }

    /**
     * Name and version of the programming language used
     */
    @Nullable
    public Language getLanguage() {
        return language;
    }

    /**
     * Name and version of the programming language used
     */
    public Service withLanguage(Language language) {
        this.language = language;
        return this;
    }

    /**
     * Representation of a service node
     */
    @Nullable
    public Node getNode() {
        return node;
    }

    /**
     * Representation of a service node
     */
    public Service withNode(Node node) {
        this.node = node;
        return this;
    }

    /**
     * Immutable name of the service emitting this event
     * (Required)
     */
    @Nullable
    public String getName() {
        return name;
    }

    /**
     * Immutable name of the service emitting this event
     * (Required)
     */
    public Service withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Environment name of the service, e.g. "production" or "staging"
     */
    @Nullable
    public String getEnvironment() {
        return environment;
    }

    /**
     * Environment name of the service, e.g. "production" or "staging"
     */
    public Service withEnvironment(String environment) {
        this.environment = environment;
        return this;
    }

    /**
     * Name and version of the language runtime running this service
     */
    @Nullable
    public RuntimeInfo getRuntime() {
        return runtime;
    }

    /**
     * Name and version of the language runtime running this service
     */
    public Service withRuntime(RuntimeInfo runtime) {
        this.runtime = runtime;
        return this;
    }

    /**
     * Version of the service emitting this event
     */
    @Nullable
    public String getVersion() {
        return version;
    }

    /**
     * Version of the service emitting this event
     */
    public Service withVersion(String version) {
        this.version = version;
        return this;
    }

}
