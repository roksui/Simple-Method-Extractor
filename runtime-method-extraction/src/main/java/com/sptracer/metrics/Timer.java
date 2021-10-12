package com.sptracer.metrics;

import com.sptracer.Recyclable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This timer track the total time and the count of invocations so that it allows for calculating weighted averages.
 */
public class Timer implements Recyclable {
    private static final double MS_IN_MICROS = TimeUnit.MILLISECONDS.toMicros(1);

    private AtomicLong totalTime = new AtomicLong();
    private AtomicLong count = new AtomicLong();

    public void update(long durationUs) {
        update(durationUs, 1);
    }

    public void update(long durationUs, long count) {
        this.totalTime.addAndGet(durationUs);
        this.count.addAndGet(count);
    }

    public long getTotalTimeUs() {
        return totalTime.get();
    }

    public double getTotalTimeMs() {
        return totalTime.get() / MS_IN_MICROS;
    }

    public long getCount() {
        return count.get();
    }

    public boolean hasContent() {
        return count.get() > 0;
    }

    @Override
    public void resetState() {
        totalTime.set(0);
        count.set(0);
    }
}
