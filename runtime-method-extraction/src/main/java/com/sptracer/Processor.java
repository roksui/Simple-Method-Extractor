package com.sptracer;


import com.sptracer.error.ErrorCapture;

public interface Processor {

    /**
     * This method is executed before the provided {@link Transaction} is reported.
     *
     * @param transaction The transaction to process.
     */
    void processBeforeReport(Transaction transaction);

    /**
     * This method is executed before the provided {@link ErrorCapture} is reported.
     *
     * @param error The error to process.
     */
    void processBeforeReport(ErrorCapture error);
}