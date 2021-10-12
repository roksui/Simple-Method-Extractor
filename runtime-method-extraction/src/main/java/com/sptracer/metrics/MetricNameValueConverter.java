package com.sptracer.metrics;

import com.sptracer.configuration.converter.AbstractValueConverter;

public class MetricNameValueConverter extends AbstractValueConverter<MetricName> {
    @Override
    public MetricName convert(String s) throws IllegalArgumentException {
        return MetricName.name(s).build();
    }

    @Override
    public String toString(MetricName value) {
        return value.getName();
    }
}
