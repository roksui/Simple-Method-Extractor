package com.sptracer.configuration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeDuration implements Comparable<TimeDuration> {

    public static final Pattern DURATION_PATTERN = Pattern.compile("^(-)?(\\d+)(ms|s|m)$");
    private final String durationString;

    private final long durationMs;

    private TimeDuration(String durationString, long durationMs) {
        this.durationString = durationString;
        this.durationMs = durationMs;
    }

    public static TimeDuration of(String durationString) {
        Matcher matcher = DURATION_PATTERN.matcher(durationString);
        if (matcher.matches()) {
            long duration = Long.parseLong(matcher.group(2));
            if (matcher.group(1) != null) {
                duration = duration * -1;
            }
            return new TimeDuration(durationString, duration * getDurationMultiplier(matcher.group(3)));
        } else {
            throw new IllegalArgumentException("Invalid duration '" + durationString + "'");
        }
    }

    private static int getDurationMultiplier(String unit) {
        switch (unit) {
            case "ms":
                return 1;
            case "s":
                return 1000;
            case "m":
                return 1000 * 60;
            default:
                throw new IllegalStateException("Duration unit '" + unit + "' is unknown");
        }
    }

    public long getMillis() {
        return durationMs;
    }

    @Override
    public String toString() {
        return durationString;
    }

    @Override
    public int compareTo(TimeDuration o) {
        return Long.compare(durationMs, o.durationMs);
    }
}
