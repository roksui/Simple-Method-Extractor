package com.sptracer;

import com.sptracer.util.ClassUtils;
import net.bytebuddy.matcher.ElementMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationClassLoaderMatcher extends ElementMatcher.Junction.AbstractBase<ClassLoader> {

    private static final boolean DEBUG_INSTRUMENTATION = SpTracer.getPlugin(CorePlugin.class).isDebugInstrumentation();

    private static final Logger logger = LoggerFactory.getLogger(ApplicationClassLoaderMatcher.class);

    @Override
    public boolean matches(ClassLoader target) {
        // only returns true if this class was loaded by the provided classLoader or by a parent of it
        // i.e. only if it is from the same application
        final boolean result = ClassUtils.loadClassOrReturnNull(target, "org.stagemonitor.core.Stagemonitor") == SpTracer.class;
        if (DEBUG_INSTRUMENTATION) {
            logger.info("Instrumenting ClassLoader {}: {}", target, result);
        }
        return result;
    }
}
