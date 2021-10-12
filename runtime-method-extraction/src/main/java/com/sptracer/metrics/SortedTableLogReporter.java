package com.sptracer.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import com.sptracer.SpTracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.sptracer.metrics.MetricName.name;

public class SortedTableLogReporter extends ScheduledMetrics2Reporter {

    private static final int CONSOLE_WIDTH = 80;
    private static final MetricName reportingTimeMetricName = name("reporting_time").tag("reporter", "log").build();

    private final Locale locale;
    private final Logger log;

    /**
     * Returns a new {@link SortedTableLogReporter.Builder} for {@link SortedTableLogReporter}.
     *
     * @param registry the registry to report
     * @return a {@link SortedTableLogReporter.Builder} instance for a {@link SortedTableLogReporter}
     */
    public static Builder forRegistry(Metric2Registry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link SortedTableLogReporter} instances. Defaults to using the
     * default locale and time zone, writing to {@code System.out}, converting
     * rates to events/second, converting durations to milliseconds, and not
     * filtering metrics.
     */
    public static class Builder extends ScheduledMetrics2Reporter.Builder<SortedTableLogReporter, Builder> {
        private Logger log = LoggerFactory.getLogger("metrics");
        private Locale locale = Locale.getDefault();

        private Builder(Metric2Registry registry) {
            super(registry, "stagemonitor-log-reporter");
        }

        /**
         * Log to the given {@link Logger}.
         *
         * @param log a {@link Logger} instance.
         * @return {@code this}
         */
        public Builder log(Logger log) {
            this.log = log;
            return this;
        }

        /**
         * Format numbers for the given {@link java.util.Locale}.
         *
         * @param locale a {@link java.util.Locale}
         * @return {@code this}
         */
        public Builder formattedFor(Locale locale) {
            this.locale = locale;
            return this;
        }

        public SortedTableLogReporter build() {
            return new SortedTableLogReporter(this);
        }
    }

    private SortedTableLogReporter(Builder builder) {
        super(builder);
        this.log = builder.log;
        this.locale = builder.locale;
    }

    @Override
    public void reportMetrics(Map<MetricName, Gauge> gauges,
                              Map<MetricName, Counter> counters,
                              Map<MetricName, Histogram> histograms,
                              Map<MetricName, Meter> meters,
                              Map<MetricName, Timer> timers) {

        final Timer.Context time = SpTracer.getMetric2Registry().timer(reportingTimeMetricName).time();

        StringBuilder sb = new StringBuilder(1000);
        printWithBanner("Metrics", '=', sb);
        sb.append('\n');

        try {
            sb.append('\n');
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info(sb.toString());
            time.stop();
        }
    }

    private static <K, V> Map<K, V> sortByValue(Map<K, V> map, final Comparator<V> valueComparator) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return valueComparator.compare(o1.getValue(), o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private void printGauge(String name, Gauge gauge, int maxNameLength, StringBuilder sb) {
        sb.append(String.format("%" + maxNameLength + "s | ", name));
        sb.append(gauge.getValue()).append('\n');

    }

    private void printCounter(String name, Counter counter, int maxNameLength, StringBuilder sb) {
        sb.append(String.format("%" + maxNameLength + "s | ", name));
        sb.append(counter.getCount()).append('\n');
    }

    private void printMeter(String name, Meter meter, int maxNameLength, StringBuilder sb) {
        sb.append(String.format("%" + maxNameLength + "s | ", name));
        sb.append(formatCount(meter.getCount()));
        printMetered(meter, sb);
        sb.append('\n');
    }

    private void printMetered(Metered metered, StringBuilder sb) {
        printDouble(convertRate(metered.getMeanRate()), sb);
        printDouble(convertRate(metered.getOneMinuteRate()), sb);
        printDouble(convertRate(metered.getFiveMinuteRate()), sb);
        printDouble(convertRate(metered.getFifteenMinuteRate()), sb);
        sb.append(String.format("%-13s | ", getRateUnit()));
        sb.append(getDurationUnit());
    }


    private void printHistogram(String name, Histogram histogram, int maxNameLength, StringBuilder sb) {
        sb.append(String.format("%" + maxNameLength + "s | ", name));
        sb.append(formatCount(histogram.getCount()));
        printHistogramSnapshot(histogram.getSnapshot(), sb);
        sb.append('\n');
    }

    private void printTimerSnapshot(Snapshot snapshot, StringBuilder sb) {
        printDouble(convertDuration(snapshot.getMean()), sb);
        printDouble(convertDuration(snapshot.getMin()), sb);
        printDouble(convertDuration(snapshot.getMax()), sb);
        printDouble(convertDuration(snapshot.getStdDev()), sb);
        printDouble(convertDuration(snapshot.getMedian()), sb);
        printDouble(convertDuration(snapshot.get75thPercentile()), sb);
        printDouble(convertDuration(snapshot.get95thPercentile()), sb);
        printDouble(convertDuration(snapshot.get98thPercentile()), sb);
        printDouble(convertDuration(snapshot.get99thPercentile()), sb);
        printDouble(convertDuration(snapshot.get999thPercentile()), sb);
    }

    private void printHistogramSnapshot(Snapshot snapshot, StringBuilder sb) {
        printDouble(snapshot.getMean(), sb);
        printDouble(snapshot.getMin(), sb);
        printDouble(snapshot.getMax(), sb);
        printDouble(snapshot.getStdDev(), sb);
        printDouble(snapshot.getMedian(), sb);
        printDouble(snapshot.get75thPercentile(), sb);
        printDouble(snapshot.get95thPercentile(), sb);
        printDouble(snapshot.get98thPercentile(), sb);
        printDouble(snapshot.get99thPercentile(), sb);
        printDouble(snapshot.get999thPercentile(), sb);
    }

    private void printTimer(String name, Timer timer, int maxNameLength, StringBuilder sb) {
        final Snapshot snapshot = timer.getSnapshot();
        sb.append(String.format("%" + maxNameLength + "s | ", name));
        sb.append(formatCount(timer.getCount()));
        printTimerSnapshot(snapshot, sb);
        printMetered(timer, sb);
        sb.append('\n');
    }

    private String formatCount(long count) {
        return String.format(locale, "%,9d | ", count);
    }

    public void printDouble(double d, StringBuilder sb) {
        sb.append(String.format(locale, "%,9.2f | ", d));
    }

    private void printWithBanner(String s, char c, StringBuilder sb) {
        sb.append(s);
        sb.append(' ');
        for (int i = 0; i < (CONSOLE_WIDTH - s.length() - 1); i++) {
            sb.append(c);
        }
        sb.append('\n');
    }

}
