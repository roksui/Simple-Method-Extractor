package com.sptracer.impl;

import com.sptracer.Recyclable;
import com.sptracer.util.BinaryHeaderMap;
import com.sptracer.util.NoRandomAccessMap;

import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * A garbage-free data structure for text and binary headers
 */
public class Headers implements Recyclable, Iterable<Headers.Header> {
    private final NoRandomAccessMap<String, String> textHeaders = new NoRandomAccessMap<>();
    private final BinaryHeaderMap binaryHeaders = new BinaryHeaderMap();
    private final NoGarbageIterator iterator = new NoGarbageIterator();

    /**
     * Adds text header.
     *
     * @param key   header key, will be ignored if {@literal null}
     * @param value header value, can be {@literal null}
     */
    public void add(@Nullable String key, @Nullable String value) {
        if (key == null) {
            return;
        }
        textHeaders.add(key, value);
    }

    /**
     * Adds binary header.
     *
     * @param key   header key, will be ignored if {@literal null}
     * @param value header value, can be {@literal null}
     */
    public void add(@Nullable String key, @Nullable byte[] value) {
        if (key == null) {
            return;
        }
        binaryHeaders.add(key, value);
    }

    @Override
    public void resetState() {
        textHeaders.resetState();
        binaryHeaders.resetState();
    }

    public int size() {
        return textHeaders.size() + binaryHeaders.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<Header> iterator() {
        iterator.reset();
        return iterator;
    }

    public void copyFrom(Headers other) {
        textHeaders.copyFrom(other.textHeaders);
        binaryHeaders.copyFrom(other.binaryHeaders);
    }

    public interface Header {
        String getKey();

        @Nullable
        CharSequence getValue();
    }

    private static class HeaderImpl implements Header {
        @Nullable
        String key;
        @Nullable
        CharSequence value;

        public String getKey() {
            if (key == null) {
                throw new IllegalStateException("Key shouldn't be null. Make sure you don't read and write to this map concurrently");
            }
            return key;
        }

        @Nullable
        public CharSequence getValue() {
            return value;
        }

        void reset() {
            key = null;
            value = null;
        }
    }

    private class NoGarbageIterator implements Iterator<Header> {
        @SuppressWarnings("NotNullFieldNotInitialized")
        private Iterator<NoRandomAccessMap.Entry<String, String>> textHeadersIterator;
        @SuppressWarnings("NotNullFieldNotInitialized")
        private Iterator<BinaryHeaderMap.Entry> binaryHeadersIterator;
        private final HeaderImpl header = new HeaderImpl();

        @Override
        public boolean hasNext() {
            return textHeadersIterator.hasNext() || binaryHeadersIterator.hasNext();
        }

        @Override
        public Header next() {
            if (textHeadersIterator.hasNext()) {
                NoRandomAccessMap.Entry<String, String> textHeader = textHeadersIterator.next();
                header.key = textHeader.getKey();
                header.value = textHeader.getValue();
            } else if (binaryHeadersIterator.hasNext()) {
                BinaryHeaderMap.Entry binaryHeader = binaryHeadersIterator.next();
                header.key = binaryHeader.getKey();
                header.value = binaryHeader.getValue();
            } else {
                throw new IllegalStateException("next() called on a depleted iterator");
            }
            return header;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        void reset() {
            textHeadersIterator = textHeaders.iterator();
            binaryHeadersIterator = binaryHeaders.iterator();
            header.reset();
        }
    }

}
