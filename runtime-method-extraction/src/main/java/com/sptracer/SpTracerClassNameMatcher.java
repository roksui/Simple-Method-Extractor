package com.sptracer;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.Collection;

import static net.bytebuddy.matcher.ElementMatchers.isSubTypeOf;
import static net.bytebuddy.matcher.ElementMatchers.not;

public class SpTracerClassNameMatcher extends ElementMatcher.Junction.AbstractBase<TypeDescription> {

    private static final Logger logger = LoggerFactory.getLogger(SpTracerClassNameMatcher.class);

    private static Collection<String> includes;

    private static Collection<String> excludes;

    private static Collection<String> excludeContaining;

    public static final SpTracerClassNameMatcher INSTANCE = new SpTracerClassNameMatcher();

    public static ElementMatcher.Junction<TypeDescription> isInsideMonitoredProject() {
        return INSTANCE.and(not(isSubTypeOf(SpTracerByteBuddyTransformer.class)));
    }

    private SpTracerClassNameMatcher() {
    }

    static {
        initIncludesAndExcludes();
    }

    private static void initIncludesAndExcludes() {
        CorePlugin corePlugin = SpTracer.getPlugin(CorePlugin.class);

        excludeContaining = new ArrayList<String>(corePlugin.getExcludeContaining().size());
        excludeContaining.addAll(corePlugin.getExcludeContaining());

        excludes = new ArrayList<String>(corePlugin.getExcludePackages().size());
        excludes.add("org.stagemonitor");
        excludes.addAll(corePlugin.getExcludePackages());

        includes = new ArrayList<String>(corePlugin.getIncludePackages().size());
        includes.addAll(corePlugin.getIncludePackages());
        if (includes.isEmpty()) {
            logger.warn("No includes for instrumentation configured. Please set the stagemonitor.instrument.include property.");
        }
    }

    /**
     * Checks if a specific class should be instrumented with this instrumenter
     * <p>
     * The default implementation considers the following properties:
     * <ul>
     * <li><code>stagemonitor.instrument.excludeContaining</code></li>
     * <li><code>stagemonitor.instrument.include</code></li>
     * <li><code>stagemonitor.instrument.exclude</code></li>
     * </ul>
     *
     * @param className The name of the class. For example java/lang/String
     * @return <code>true</code>, if the class should be instrumented, <code>false</code> otherwise
     */
    public static boolean isIncluded(String className) {
        for (String exclude : excludeContaining) {
            if (className.contains(exclude)) {
                return false;
            }
        }

        for (String include : includes) {
            if (className.startsWith(include)) {
                return !hasMoreSpecificExclude(className, include);
            }
        }
        return false;
    }

    private static boolean hasMoreSpecificExclude(String className, String include) {
        for (String exclude : excludes) {
            if (exclude.length() > include.length() && exclude.startsWith(include) && className.startsWith(exclude)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(TypeDescription target) {
        return isIncluded(target.getName());
    }
}
