package com.sptracer;

public interface ObjectPool<T> {

    /**
     * Tries to reuse any existing instance if pool has any, otherwise creates a new un-pooled instance
     *
     * @return object instance, either from pool or freshly allocated
     */
    T createInstance();

    /**
     * Recycles an object
     *
     * @param obj object to recycle
     */
    void recycle(T obj);

    /**
     * @return number of available objects in pool
     */
    int getObjectsInPool();

    /**
     * @return number of times that objects could not be returned to the pool because the pool was already full
     */
    long getGarbageCreated();

    void clear();
}
