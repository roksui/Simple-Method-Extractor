package com.sptracer.util;

public final class ThreadUtils {

    public static final String ELASTIC_APM_THREAD_PREFIX = "elastic-apm-";

    private ThreadUtils() {
    }

    public static String addElasticApmThreadPrefix(String purpose) {
        return ELASTIC_APM_THREAD_PREFIX + purpose;
    }
}
