package com.sptracer;

import com.codahale.metrics.Counter;
import com.sptracer.metrics.Metric2Registry;
import net.bytebuddy.matcher.ElementMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.sptracer.metrics.MetricName.name;


public class TimedElementMatcherDecorator<T> implements ElementMatcher<T> {

    private static final Logger logger = LoggerFactory.getLogger(TimedElementMatcherDecorator.class);
    private static final boolean DEBUG_INSTRUMENTATION = SpTracer.getConfiguration().getConfig(CorePlugin.class).isDebugInstrumentation();

    private static final Metric2Registry timeRegistry = new Metric2Registry();
    private static final Metric2Registry countRegistry = new Metric2Registry();
    private final ElementMatcher<T> delegate;

    private final Counter count;
    private final Counter time;

    public static <T> ElementMatcher<T> timed(String type, String transformerName, ElementMatcher<T> delegate) {
        if (DEBUG_INSTRUMENTATION) {
            return new TimedElementMatcherDecorator<T>(delegate, type, transformerName);
        } else {
            return delegate;
        }
    }

    private TimedElementMatcherDecorator(ElementMatcher<T> delegate, String type, String transformerName) {
        this.delegate = delegate;
        this.count = countRegistry
                .counter(name("element_matcher").type(type).tag("transformer", transformerName).build());
        this.time = timeRegistry
                .counter(name("element_matcher").type(type).tag("transformer", transformerName).build());
    }

    @Override
    public boolean matches(T target) {
        long start = System.nanoTime();
        try {
            return delegate.matches(target);
        } finally {
            count.inc();
            time.inc(System.nanoTime() - start);
        }
    }

}