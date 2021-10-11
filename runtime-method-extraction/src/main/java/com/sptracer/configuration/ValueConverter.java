package com.sptracer.configuration;

public interface ValueConverter<T> {

    T convert(String s) throws IllegalArgumentException;

    String toString(T value);

    String toSafeString(T value);
}
