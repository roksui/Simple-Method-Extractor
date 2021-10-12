package com.sptracer.impl;

import com.sptracer.*;
import org.jctools.queues.atomic.MpmcAtomicArrayQueue;

import javax.annotation.Nullable;

import static com.sptracer.impl.AbstractContext.REDACTED_CONTEXT_STRING;

public class Message implements Recyclable {

    private static final ObjectPool<StringBuilder> stringBuilderPool = QueueBasedObjectPool.of(new MpmcAtomicArrayQueue<StringBuilder>(128), false,
            new Allocator<StringBuilder>() {
                @Override
                public StringBuilder createInstance() {
                    return new StringBuilder();
                }
            },
            new Resetter<StringBuilder>() {
                @Override
                public void recycle(StringBuilder object) {
                    object.setLength(0);
                }
            });

    @Nullable
    private String queueName;

    @Nullable
    private StringBuilder body;

    /**
     * Represents the message age in milliseconds. Since 0 is a valid value (can occur due to clock skews between
     * sender and receiver) - a negative value represents invalid or unavailable age.
     */
    private long age = -1L;

    /**
     * A mapping of message headers (in JMS includes properties as well)
     */
    private final Headers headers = new Headers();

    @Nullable
    public String getQueueName() {
        return queueName;
    }

    public Message withQueue(String queueName) {
        this.queueName = queueName;
        return this;
    }

    /**
     * Gets a body StringBuilder to write content to. If this message's body is not initializes, this method will
     * initialize from the StringBuilder pool.
     *
     * @return a StringBuilder to write body content to. Never returns null.
     */
    public StringBuilder getBodyForWrite() {
        if (body == null) {
            body = stringBuilderPool.createInstance();
        }
        return body;
    }

    /**
     * @return a body if already initialized, null otherwise
     */
    @Nullable
    public StringBuilder getBodyForRead() {
        return body;
    }

    public Message withBody(@Nullable String body) {
        StringBuilder thisBody = getBodyForWrite();
        thisBody.setLength(0);
        thisBody.append(body);
        return this;
    }

    public Message appendToBody(CharSequence bodyContent) {
        getBodyForWrite().append(bodyContent);
        return this;
    }

    public void redactBody() {
        if (body != null && body.length() > 0) {
            body.setLength(0);
            body.append(REDACTED_CONTEXT_STRING);
        }
    }

    public Message addHeader(@Nullable String key, @Nullable String value) {
        headers.add(key, value);
        return this;
    }

    public Message addHeader(@Nullable String key, @Nullable byte[] value) {
        headers.add(key, value);
        return this;
    }

    public long getAge() {
        return age;
    }

    @SuppressWarnings("UnusedReturnValue")
    public Message withAge(long age) {
        this.age = age;
        return this;
    }

    public Headers getHeaders() {
        return headers;
    }

    public boolean hasContent() {
        return queueName != null || (body != null && body.length() > 0) || headers.size() > 0;
    }

    @Override
    public void resetState() {
        queueName = null;
        headers.resetState();
        age = -1L;
        if (body != null) {
            stringBuilderPool.recycle(body);
            body = null;
        }
    }

    public void copyFrom(Message other) {
        resetState();
        this.queueName = other.getQueueName();
        if (other.body != null) {
            getBodyForWrite().append(other.body);
        }
        this.headers.copyFrom(other.getHeaders());
        this.age = other.getAge();
    }
}
