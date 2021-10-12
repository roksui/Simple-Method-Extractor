package com.sptracer.impl;

public interface HeaderSetter<T, C> {

    void setHeader(String headerName, T headerValue, C carrier);
}
