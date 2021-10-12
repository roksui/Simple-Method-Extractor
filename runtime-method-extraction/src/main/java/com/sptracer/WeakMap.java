package com.sptracer;

import com.sun.istack.internal.Nullable;

import java.util.Map;

public interface WeakMap<K, V> extends Iterable<Map.Entry<K, V>> {

    @Nullable
    V get(K key);

    @Nullable
    V put(K key, V value);

    @Nullable
    V remove(K key);

    boolean containsKey(K process);

    void clear();

    @Nullable
    V putIfAbsent(K key, V value);

    int approximateSize();

    interface DefaultValueSupplier<K, V> {
        @Nullable
        V getDefaultValue(K key);
    }
}
