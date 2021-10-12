package com.sptracer.metrics;

import com.codahale.metrics.Clock;

public class QuantizedClock extends Clock {
    private final Clock delegate;
    private final long periodInMS;

    public QuantizedClock(Clock delegate, long periodInMS) {
        this.delegate = delegate;
        this.periodInMS = periodInMS;
    }

    @Override
    public long getTick() {
        return delegate.getTick();
    }

    @Override
    public long getTime() {
        final long time = delegate.getTime();
        return time - (time % periodInMS);
    }
}
