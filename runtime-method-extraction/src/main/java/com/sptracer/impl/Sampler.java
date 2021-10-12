package com.sptracer.impl;

import com.sptracer.Id;

public interface Sampler {

    /**
     * Determines whether the given transaction should be sampled.
     *
     * @param traceId The id of the transaction.
     * @return The sampling decision.
     */
    boolean isSampled(Id traceId);

    /**
     * @return current sample rate
     */
    double getSampleRate();


    /**
     * @return sample rate as (constant) header for context propagation
     *
     * <p>
     * While the {@code tracestate} header is not related to sampler itself, putting this here allows to reuse the same
     * {@link String} instance as long as the sample rate does not change to minimize allocation
     * </p>
     */
    String getTraceStateHeader();
}
