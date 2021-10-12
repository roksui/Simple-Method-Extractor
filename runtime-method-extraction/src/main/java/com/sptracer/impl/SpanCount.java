package com.sptracer.impl;

import com.sptracer.Recyclable;

import java.util.concurrent.atomic.AtomicInteger;

public class SpanCount implements Recyclable {

    private final AtomicInteger dropped = new AtomicInteger(0);
    private final AtomicInteger reported = new AtomicInteger(0);
    private final AtomicInteger total = new AtomicInteger(0);

    public AtomicInteger getDropped() {
        return dropped;
    }

    public AtomicInteger getReported() {
        return reported;
    }

    public AtomicInteger getTotal() {
        return total;
    }

    public boolean isSpanLimitReached(int maxSpans) {
        return maxSpans <= total.get() - dropped.get();
    }

    @Override
    public void resetState() {
        dropped.set(0);
        reported.set(0);
        total.set(0);
    }
}
