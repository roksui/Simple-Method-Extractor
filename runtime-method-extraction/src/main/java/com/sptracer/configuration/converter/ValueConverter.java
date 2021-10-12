package com.sptracer.configuration.converter;

public interface ValueConverter<T> {

    T convert(String s) throws IllegalArgumentException;

    String toString(T value);

    String toSafeString(T value);
}
