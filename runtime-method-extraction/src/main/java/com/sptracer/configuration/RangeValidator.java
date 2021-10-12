package com.sptracer.configuration;

import javax.annotation.Nullable;

public class RangeValidator<T extends Comparable> implements ConfigurationOption.Validator<T> {

    @Nullable
    private final T min;
    @Nullable
    private final T max;
    private final boolean mustBeInRange;

    private RangeValidator(@Nullable T min, @Nullable T max, boolean mustBeInRange) {
        this.min = min;
        this.max = max;
        this.mustBeInRange = mustBeInRange;
    }

    public static <T extends Comparable> RangeValidator<T> isInRange(T min, T max) {
        return new RangeValidator<>(min, max, true);
    }

    public static <T extends Comparable> RangeValidator<T> isNotInRange(T min, T max) {
        return new RangeValidator<>(min, max, false);
    }

    public static <T extends Comparable> RangeValidator<T> min(T min) {
        return new RangeValidator<>(min, null, true);
    }

    public static <T extends Comparable> RangeValidator<T> max(T max) {
        return new RangeValidator<>(null, max, true);
    }

    @Override
    public void assertValid(@Nullable T value) {
        if (value != null) {
            boolean isInRange = true;
            if (min != null) {
                isInRange = min.compareTo(value) <= 0;
            }
            if (max != null) {
                isInRange &= value.compareTo(max) <= 0;
            }

            if (!isInRange && mustBeInRange) {
                throw new IllegalArgumentException(value + " must be in the range [" + min + "," + max + "]");
            }

            if (isInRange && !mustBeInRange) {
                throw new IllegalArgumentException(value + " must not be in the range [" + min + "," + max + "]");
            }
        }
    }
}
