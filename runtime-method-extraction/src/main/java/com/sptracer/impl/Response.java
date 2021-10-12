package com.sptracer.impl;


import com.sptracer.Recyclable;
import com.sptracer.util.PotentiallyMultiValuedMap;

import javax.annotation.Nullable;
import java.util.Collection;

public class Response implements Recyclable {

    /**
     * A mapping of HTTP headers of the response object
     */
    private final PotentiallyMultiValuedMap headers = new PotentiallyMultiValuedMap();
    /**
     * A boolean indicating whether the response was finished or not
     */
    private boolean finished;
    private boolean headersSent;
    /**
     * The HTTP status code of the response.
     */
    private int statusCode;

    /**
     * A boolean indicating whether the response was finished or not
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * A boolean indicating whether the response was finished or not
     */
    public Response withFinished(boolean finished) {
        this.finished = finished;
        return this;
    }

    /**
     * Adds a response header.
     *
     * @param headerName  The name of the header.
     * @param headerValue The value of the header.
     * @return {@code this}, for fluent method chaining
     */
    public Response addHeader(String headerName, String headerValue) {
        headers.add(headerName, headerValue);
        return this;
    }

    public Response addHeader(String headerName, @Nullable Collection<String> headerValues) {
        if (headerValues != null) {
            for (String headerValue : headerValues) {
                headers.add(headerName, headerValue);
            }
        }
        return this;
    }

    /**
     * A mapping of HTTP headers of the response object
     */
    public PotentiallyMultiValuedMap getHeaders() {
        return headers;
    }


    public boolean isHeadersSent() {
        return headersSent;
    }

    public Response withHeadersSent(boolean headersSent) {
        this.headersSent = headersSent;
        return this;
    }

    /**
     * The HTTP status code of the response.
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * The HTTP status code of the response.
     */
    public Response withStatusCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    @Override
    public void resetState() {
        finished = false;
        headers.resetState();
        headersSent = false;
        statusCode = 0;
    }

    public void copyFrom(Response other) {
        this.finished = other.finished;
        this.headers.copyFrom(other.headers);
        this.headersSent = other.headersSent;
        this.statusCode = other.statusCode;
    }

    public boolean hasContent() {
        return statusCode > 0 || headers.size() > 0;
    }
}