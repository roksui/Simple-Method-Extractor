package com.sptracer.impl;

import com.sptracer.Recyclable;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractContext implements Recyclable {

    public static final String REDACTED_CONTEXT_STRING = "[REDACTED]";

    /**
     * A flat mapping of user-defined labels with {@link String} keys and {@link String}, {@link Number} or {@link Boolean} values
     * (formerly known as tags).
     * <p>
     * See also https://github.com/elastic/ecs#-base-fields
     * </p>
     */
    private final Map<String, Object> labels = new ConcurrentHashMap<>();

    /**
     * An object containing contextual data for Messages (incoming in case of transactions or outgoing in case of spans)
     */
    private final Message message = new Message();

    public Iterator<? extends Map.Entry<String, ?>> getLabelIterator() {
        return labels.entrySet().iterator();
    }

    public void addLabel(String key, String value) {
        labels.put(key, value);
    }

    public void addLabel(String key, Number value) {
        labels.put(key, value);
    }

    public void addLabel(String key, boolean value) {
        labels.put(key, value);
    }

    public Object getLabel(String key) {
        return labels.get(key);
    }

    public void clearLabels() {
        labels.clear();
    }

    public boolean hasLabels() {
        return !labels.isEmpty();
    }

    public Message getMessage() {
        return message;
    }

    @Override
    public void resetState() {
        labels.clear();
        message.resetState();
    }

    public boolean hasContent() {
        return !labels.isEmpty() || message.hasContent();
    }

    public void copyFrom(AbstractContext other) {
        labels.putAll(other.labels);
        message.copyFrom(other.message);
    }
}
