package com.sptracer.impl;

import com.sptracer.Recyclable;

import javax.annotation.Nullable;

public class Socket implements Recyclable {

    @Nullable
    private String remoteAddress;

    @Nullable
    public String getRemoteAddress() {
        return remoteAddress;
    }

    public Socket withRemoteAddress(@Nullable String remoteAddress) {
        this.remoteAddress = remoteAddress;
        return this;
    }

    @Override
    public void resetState() {
        remoteAddress = null;
    }

    public void copyFrom(Socket other) {
        this.remoteAddress = other.remoteAddress;
    }

    public boolean hasContent() {
        return remoteAddress != null;
    }
}
