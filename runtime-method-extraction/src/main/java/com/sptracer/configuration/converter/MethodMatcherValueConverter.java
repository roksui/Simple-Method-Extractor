package com.sptracer.configuration.converter;

import com.sptracer.matcher.MethodMatcher;

import static com.sptracer.ListValueConverter.COMMA_OUT_OF_BRACKETS;

public enum MethodMatcherValueConverter implements ValueConverter<MethodMatcher> {
    INSTANCE;

    @Override
    public MethodMatcher convert(String methodMatcher) throws IllegalArgumentException {
        return MethodMatcher.of(methodMatcher);
    }

    @Override
    public String toString(MethodMatcher methodMatcher) {
        return methodMatcher.toString();
    }

    @Override
    public String toSafeString(MethodMatcher value) {
        return toString(value);
    }

    public static final ListValueConverter<MethodMatcher> LIST =
            new ListValueConverter<>(INSTANCE, COMMA_OUT_OF_BRACKETS);
}
