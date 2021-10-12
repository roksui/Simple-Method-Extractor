package com.sptracer.metrics;

import com.codahale.metrics.Metered;
import com.codahale.metrics.Metric;

public class MetricsWithCountFilter implements Metric2Filter {
    @Override
    public boolean matches(MetricName name, Metric metric) {
        if (metric instanceof Metered) {
            return ((Metered) metric).getCount() > 0;
        }
        return true;
    }
}
