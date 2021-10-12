package com.sptracer.weakconcurrent;

import com.sun.istack.internal.Nullable;

public interface DetachedThreadLocal<T> {

    @Nullable
    T get();

    @Nullable
    T getAndRemove();

    void set(T value);

    void remove();

}