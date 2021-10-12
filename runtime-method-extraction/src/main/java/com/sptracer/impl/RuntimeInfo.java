package com.sptracer.impl;

/**
 * Name and version of the language runtime running this service
 */
public class RuntimeInfo {

    private final String name;
    private final String version;

    public RuntimeInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

}
