package com.sptracer.metrics;

import com.codahale.metrics.Metric;

/**
 * A filter used to determine whether or not a metric should be reported, among other things.
 */
public interface Metric2Filter {
    Metric2Filter ALL = new Metric2Filter() {
        @Override
        public boolean matches(MetricName name, Metric metric) {
            return true;
        }
    };

    boolean matches(MetricName name, Metric metric);


}
