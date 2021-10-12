package com.sptracer.configuration.converter;

import com.sptracer.configuration.ConfigurationOption;
import com.sptracer.configuration.TimeDuration;

public class TimeDurationValueConverter extends AbstractValueConverter<TimeDuration> {

    private final String defaultDurationSuffix;

    private TimeDurationValueConverter(String defaultDurationSuffix) {
        this.defaultDurationSuffix = defaultDurationSuffix;
    }

    public static TimeDurationValueConverter withDefaultDuration(String defaultDurationSuffix) {
        return new TimeDurationValueConverter(defaultDurationSuffix);
    }

    public static ConfigurationOption.ConfigurationOptionBuilder<TimeDuration> durationOption(String defaultDuration) {
        return ConfigurationOption.<TimeDuration>builder(new TimeDurationValueConverter(defaultDuration), TimeDuration.class);
    }

    @Override
    public TimeDuration convert(String s) throws IllegalArgumentException {
        if (!s.endsWith("ms") && !s.endsWith("s") && !s.endsWith("m")) {
            s += defaultDurationSuffix;
        }
        return TimeDuration.of(s);
    }

    @Override
    public String toString(TimeDuration value) {
        return value.toString();
    }

    public String getDefaultDurationSuffix() {
        return defaultDurationSuffix;
    }
}
