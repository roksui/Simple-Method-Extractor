package com.sptracer;

public class EpochTickClock implements Recyclable {

    private long nanoTimeOffsetToEpoch;

    /**
     * Initializes the clock by aligning the {@link #nanoTimeOffsetToEpoch offset} with the offset of another clock.
     *
     * @param other the other clock, which has already been initialized
     */
    public void init(EpochTickClock other) {
        this.nanoTimeOffsetToEpoch = other.nanoTimeOffsetToEpoch;
    }

    /**
     * Initializes and calibrates the clock based on wall clock time
     *
     * @return the epoch microsecond timestamp at initialization time
     */
    public long init() {
        return init(System.currentTimeMillis() * 1000, System.nanoTime());
    }

    void init(long nanoTimeOffsetToEpoch) {
        this.nanoTimeOffsetToEpoch = nanoTimeOffsetToEpoch;
    }

    /**
     * Initializes and calibrates the clock based on wall clock time
     *
     * @param epochMicrosWallClock the current timestamp in microseconds since epoch, based on wall clock time
     * @param nanoTime             the current nanosecond ticks (mostly {@link System#nanoTime()}
     * @return the epoch microsecond timestamp at initialization time
     */
    public long init(long epochMicrosWallClock, long nanoTime) {
        nanoTimeOffsetToEpoch = epochMicrosWallClock * 1_000 - nanoTime;
        return epochMicrosWallClock;
    }

    public long getEpochMicros() {
        return getEpochMicros(System.nanoTime());
    }

    public long getEpochMicros(final long nanoTime) {
        return (nanoTime + nanoTimeOffsetToEpoch) / 1000;
    }

    @Override
    public void resetState() {
        nanoTimeOffsetToEpoch = 0;
    }

    long getOffset() {
        return nanoTimeOffsetToEpoch;
    }
}
