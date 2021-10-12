package com.sptracer;

public interface Recyclable {

    /**
     * resets pooled object state so it can be reused
     */
    void resetState();
}
