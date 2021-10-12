package com.sptracer.impl;

/**
 * Name and version of the programming language used
 */
public class Language {

    /**
     * (Required)
     */
    private final String name;
    private final String version;

    public Language(String name, String version) {
        this.name = name;
        this.version = version;
    }

    /**
     * (Required)
     */
    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

}
