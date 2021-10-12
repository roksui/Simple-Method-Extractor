package com.sptracer;

public interface Allocator<T> {

    /**
     * @return new instance of pooled object type
     */
    T createInstance();
}
