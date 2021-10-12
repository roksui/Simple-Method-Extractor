package com.sptracer.impl;

import com.sptracer.Id;

/**
 * This is a implementation of {@link Sampler} which always returns the same sampling decision.
 */
public class ConstantSampler implements Sampler {

    private static final Sampler TRUE = new ConstantSampler(true);
    private static final Sampler FALSE = new ConstantSampler(false);

    private final boolean decision;
    private final double rate;

    private final String traceStateHeader;

    private ConstantSampler(boolean decision) {
        this.decision = decision;
        this.rate = decision ? 1.0d : 0.0d;
        this.traceStateHeader = TraceState.getHeaderValue(rate);
    }

    public static Sampler of(boolean decision) {
        if (decision) {
            return TRUE;
        } else {
            return FALSE;
        }
    }

    @Override
    public boolean isSampled(Id traceId) {
        return decision;
    }

    @Override
    public double getSampleRate() {
        return rate;
    }

    @Override
    public String getTraceStateHeader() {
        return traceStateHeader;
    }
}
