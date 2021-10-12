package com.sptracer.impl;

import javax.annotation.Nullable;

/**
 * A representation of a service node, ie JVM
 */
public class Node {

    /**
     * (Optional)
     * A name representing this JVM. Should be unique within the service.
     */
    @Nullable
    private final String name;

    public Node(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public boolean hasContents() {
        return name != null && !name.isEmpty();
    }
}
