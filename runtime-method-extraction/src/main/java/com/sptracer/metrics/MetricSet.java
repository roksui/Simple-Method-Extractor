package com.sptracer.metrics;

import com.sptracer.Recyclable;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;


public class MetricSet implements Recyclable {
    private final Labels.Immutable labels;
    private final ConcurrentMap<String, DoubleSupplier> gauges;
    // low load factor as hash collisions are quite costly when tracking breakdown metrics
    private final ConcurrentMap<String, Timer> timers = new ConcurrentHashMap<>(32, 0.5f, Runtime.getRuntime().availableProcessors());
    private final ConcurrentMap<String, AtomicLong> counters = new ConcurrentHashMap<>(32, 0.5f, Runtime.getRuntime().availableProcessors());
    private volatile boolean hasNonEmptyTimer;
    private volatile boolean hasNonEmptyCounter;

    MetricSet(Labels.Immutable labels) {
        this(labels, new ConcurrentHashMap<String, DoubleSupplier>());
    }

    MetricSet(Labels.Immutable labels, ConcurrentMap<String, DoubleSupplier> gauges) {
        this.labels = labels;
        this.gauges = gauges;
    }

    void addGauge(String name, DoubleSupplier metric) {
        gauges.putIfAbsent(name, metric);
    }

    @Nullable
    DoubleSupplier getGauge(String name) {
        return gauges.get(name);
    }

    public Labels getLabels() {
        return labels;
    }

    public ConcurrentMap<String, DoubleSupplier> getGauges() {
        return gauges;
    }

    public Timer timer(String timerName) {
        hasNonEmptyTimer = true;
        Timer timer = timers.get(timerName);
        if (timer == null) {
            timers.putIfAbsent(timerName, new Timer());
            timer = timers.get(timerName);
        }
        return timer;
    }

    public void incrementCounter(String name) {
        hasNonEmptyCounter = true;
        AtomicLong counter = counters.get(name);
        if (counter == null) {
            counters.putIfAbsent(name, new AtomicLong());
            counter = counters.get(name);
        }
        counter.incrementAndGet();
    }

    public Map<String, Timer> getTimers() {
        return timers;
    }

    public boolean hasContent() {
        return !gauges.isEmpty() || hasNonEmptyTimer || hasNonEmptyCounter;
    }

    /**
     * Should be called only when the MetricSet is inactive
     */
    public void resetState() {
        for (Timer timer : timers.values()) {
            timer.resetState();
        }
        for (AtomicLong counter : counters.values()) {
            counter.set(0);
        }
        hasNonEmptyTimer = false;
        hasNonEmptyCounter = false;
    }

    public Map<String, AtomicLong> getCounters() {
        return counters;
    }
}
