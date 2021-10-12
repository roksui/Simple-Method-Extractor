package com.sptracer;

import com.sptracer.error.ErrorCapture;
import com.sptracer.impl.SpTracerImpl;
import com.sptracer.impl.Transaction;
import org.jctools.queues.atomic.AtomicQueueFactory;

import static org.jctools.queues.spec.ConcurrentQueueSpec.createBoundedMpmc;

public class ObjectPoolFactory {

    protected <T extends Recyclable> ObjectPool<T> createRecyclableObjectPool(int maxCapacity, Allocator<T> allocator) {
        return QueueBasedObjectPool.ofRecyclable(AtomicQueueFactory.<T>newQueue(createBoundedMpmc(maxCapacity)), false, allocator);
    }

    public ObjectPool<com.sptracer.impl.Transaction> createTransactionPool(int maxCapacity, final SpTracerImpl tracer) {
        return createRecyclableObjectPool(maxCapacity, new Allocator<com.sptracer.impl.Transaction>() {
            @Override
            public Transaction createInstance() {
                return new com.sptracer.impl.Transaction(tracer);
            }
        });
    }

    public ObjectPool<com.sptracer.impl.Span> createSpanPool(int maxCapacity, final SpTracerImpl tracer) {
        return createRecyclableObjectPool(maxCapacity, new Allocator<com.sptracer.impl.Span>() {
            @Override
            public com.sptracer.impl.Span createInstance() {
                return new com.sptracer.impl.Span(tracer);
            }
        });
    }

    public ObjectPool<ErrorCapture> createErrorPool(int maxCapacity, final SpTracerImpl tracer) {
        return createRecyclableObjectPool(maxCapacity, new Allocator<ErrorCapture>() {
            @Override
            public ErrorCapture createInstance() {
                return new ErrorCapture(tracer);
            }
        });
    }
}
