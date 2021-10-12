package com.sptracer.impl;

import com.sptracer.Recyclable;

import javax.annotation.Nullable;

public class Http implements Recyclable {

    /**
     * URL used by this HTTP outgoing span
     */
    private final Url url = new Url();

    /**
     * HTTP method used by this HTTP outgoing span
     */
    @Nullable
    private String method;

    /**
     * Status code of the response
     */
    private int statusCode;

    /**
     * URL used for the outgoing HTTP call
     */
    public CharSequence getUrl() {
        // note: do not expose the underlying Url object, as it might not have
        // all it's properties set due to providing the full URL as-is
        return url.getFull();
    }

    /**
     * @return internal {@link Url} instance
     */
    public Url getInternalUrl() {
        return url;
    }

    @Nullable
    public String getMethod() {
        return method;
    }

    public int getStatusCode() {
        return statusCode;
    }

    /**
     * URL used for the outgoing HTTP call
     */
    public Http withUrl(@Nullable String url) {
        if (url != null) {
            this.url.withFull(url);
        }
        return this;
    }

    public Http withMethod(String method) {
        this.method = method;
        return this;
    }

    public Http withStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public void resetState() {
        url.resetState();
        method = null;
        statusCode = 0;
    }

    public boolean hasContent() {
        return url.hasContent() ||
                method != null ||
                statusCode > 0;
    }

    public void copyFrom(Http other) {
        url.copyFrom(other.url);
        method = other.method;
        statusCode = other.statusCode;
    }
}
