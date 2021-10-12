package com.sptracer.impl;

public interface HeaderRemover<C> {

    void remove(String headerName, C carrier);
}
