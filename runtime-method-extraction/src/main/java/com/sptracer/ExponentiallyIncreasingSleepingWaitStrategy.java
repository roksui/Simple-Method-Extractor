package com.sptracer;

import com.lmax.disruptor.AlertException;
import com.lmax.disruptor.Sequence;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.WaitStrategy;

import java.util.concurrent.locks.LockSupport;
public final class ExponentiallyIncreasingSleepingWaitStrategy implements WaitStrategy {

    private final int sleepTimeNsStart;
    private final int sleepTimeNsMax;

    public ExponentiallyIncreasingSleepingWaitStrategy(int sleepTimeNsStart, int sleepTimeNsMax) {
        this.sleepTimeNsStart = sleepTimeNsStart;
        this.sleepTimeNsMax = sleepTimeNsMax;
    }

    @Override
    public long waitFor(final long sequence, Sequence cursor, final Sequence dependentSequence, final SequenceBarrier barrier) throws AlertException {
        long availableSequence;
        int currentSleep = sleepTimeNsStart;

        while ((availableSequence = dependentSequence.get()) < sequence) {
            currentSleep = applyWaitMethod(barrier, currentSleep);
        }

        return availableSequence;
    }

    @Override
    public void signalAllWhenBlocking() {
    }

    private int applyWaitMethod(final SequenceBarrier barrier, int currentSleep) throws AlertException {
        barrier.checkAlert();

        if (currentSleep < sleepTimeNsMax) {
            LockSupport.parkNanos(currentSleep);
            return currentSleep * 2;
        } else {
            LockSupport.parkNanos(sleepTimeNsMax);
            return currentSleep;
        }
    }
}
