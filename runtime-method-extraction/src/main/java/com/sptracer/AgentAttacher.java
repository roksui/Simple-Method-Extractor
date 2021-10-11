package com.sptracer;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.sptracer.metrics.health.ImmediateResult;
import com.sptracer.util.ClassUtils;
import com.sptracer.util.IOUtils;
import com.sptracer.util.VersionUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.scaffold.MethodGraph;
import net.bytebuddy.dynamic.scaffold.TypeValidation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

import static com.sptracer.ClassLoaderNameMatcher.classLoaderWithName;
import static com.sptracer.ClassLoaderNameMatcher.isReflectionClassLoader;
import static com.sptracer.TimedElementMatcherDecorator.timed;
import static net.bytebuddy.matcher.ElementMatchers.*;


public class AgentAttacher {

    private static final Logger logger = LoggerFactory.getLogger(AgentAttacher.class);
    private static final String DISPATCHER_CLASS_NAME = "__redirected.org.stagemonitor.dispatcher.Dispatcher";
    private static final String IGNORED_CLASSLOADERS_KEY = AgentAttacher.class.getName() + "hashCodesOfClassLoadersToIgnore";
    private static final Runnable NOOP_ON_SHUTDOWN_ACTION = new Runnable() {
        public void run() {
        }
    };

    private static final CorePlugin corePlugin = SpTracer.getPlugin(CorePlugin.class);
    private static final HealthCheckRegistry healthCheckRegistry = SpTracer.getHealthCheckRegistry();
    private static boolean runtimeAttached = false;
    private static Set<String> hashCodesOfClassLoadersToIgnore = Collections.emptySet();
    private static Instrumentation instrumentation;

    private AgentAttacher() {
    }

    /**
     * Attaches the profiler and other instrumenters at runtime so that it is not necessary to add the -javaagent
     * command line argument.
     *
     * @return A runnable that should be called on shutdown to unregister this class file transformer
     */
    public static synchronized Runnable performRuntimeAttachment() {
        if (runtimeAttached || !corePlugin.isStagemonitorActive() || !corePlugin.isAttachAgentAtRuntime()) {
            return NOOP_ON_SHUTDOWN_ACTION;
        }
        runtimeAttached = true;

        final List<ClassFileTransformer> classFileTransformers = new ArrayList<ClassFileTransformer>();
        final AutoEvictingCachingBinaryLocator binaryLocator = new AutoEvictingCachingBinaryLocator();
        if (assertNoDifferentStagemonitorVersionIsDeployedOnSameJvm() && initInstrumentation()) {
            final long start = System.currentTimeMillis();
            classFileTransformers.add(initByteBuddyClassFileTransformer(binaryLocator));
            if (corePlugin.isDebugInstrumentation()) {
                logger.info("Attached agents in {} ms", System.currentTimeMillis() - start);
            }
        }
        return new Runnable() {
            public void run() {
                for (ClassFileTransformer classFileTransformer : classFileTransformers) {
                    instrumentation.removeTransformer(classFileTransformer);
                }
                // This ClassLoader is shutting down so don't try to retransform classes of it in the future
                hashCodesOfClassLoadersToIgnore.add(ClassUtils.getIdentityString(AgentAttacher.class.getClassLoader()));
                binaryLocator.close();
            }
        };
    }

    private static boolean assertNoDifferentStagemonitorVersionIsDeployedOnSameJvm() {
        final String stagemonitorVersionKey = "stagemonitor.version";
        final String stagemonitorClassLoaderKey = "stagemonitor.classLoader";
        final String alreadyRegisteredVersion = System.getProperty(stagemonitorVersionKey);
        final String currentVersion = corePlugin.getVersion();
        if (alreadyRegisteredVersion != null && !currentVersion.equals(alreadyRegisteredVersion)) {
            final String msg = String.format("Detected a different version of stagemonitor on the same JVM:" +
                            "already registered version: %s current version: %s. " +
                            "It is not supported to have different versions of stagemonitor on the same JVM. " +
                            "For more details take a look at the javadoc.",
                    alreadyRegisteredVersion, currentVersion);
            healthCheckRegistry.register("Agent attachment", ImmediateResult.of(HealthCheck.Result.unhealthy(msg)));
            return false;
        }
        if (currentVersion != null) {
            System.setProperty(stagemonitorVersionKey, currentVersion);
            System.setProperty(stagemonitorClassLoaderKey, SpTracer.class.getClassLoader().toString());
        }
        return true;
    }

    private static boolean initInstrumentation() {
        healthCheckRegistry.register("Agent attachment", ImmediateResult.of(HealthCheck.Result.unhealthy("Unknown error")));
        try {
            instrumentation = getInstrumentation();
            healthCheckRegistry.register("Agent attachment", ImmediateResult.of(HealthCheck.Result.healthy()));
            ensureDispatcherIsAppendedToBootstrapClasspath(instrumentation);
            Dispatcher.getValues().putIfAbsent(IGNORED_CLASSLOADERS_KEY, Collections.newSetFromMap(new ConcurrentHashMap<Integer, Boolean>()));
            hashCodesOfClassLoadersToIgnore = Dispatcher.get(IGNORED_CLASSLOADERS_KEY);
            return true;
        } catch (Throwable e) {
            final String msg = "Failed to perform runtime attachment of the stagemonitor agent. Make sure that you run your " +
                    "application with a JDK (not a JRE)." +
                    "To make stagemonitor work with a JRE, you have to add the following command line argument to the " +
                    "start of the JVM: -javaagent:/path/to/byte-buddy-agent-<version>.jar. " +
                    "The version of the agent depends on the version of stagemonitor. " +
                    "You can download the appropriate agent for the stagemonitor version you are using here: " + getByteBuddyAgentDownloadUrl();
            healthCheckRegistry.register("Agent attachment", ImmediateResult.of(HealthCheck.Result.unhealthy(msg)));
            logger.warn(msg, e);
            return false;
        }
    }

    private static Instrumentation getInstrumentation() {
        try {
            return ByteBuddyAgent.getInstrumentation();
        } catch (IllegalStateException e) {
            return ByteBuddyAgent.install(
                    new ByteBuddyAgent.AttachmentProvider.Compound(
                            new EhCacheAttachmentProvider(),
                            ByteBuddyAgent.AttachmentProvider.DEFAULT));
        }
    }

    private static String getByteBuddyAgentDownloadUrl() {
        final String groupId = "net.bytebuddy";
        final String byteBuddyVersion = VersionUtils.getVersionFromPomProperties(ByteBuddy.class, groupId, "byte-buddy");
        return VersionUtils.getMavenCentralDownloadLink(groupId, "byte-buddy-agent", byteBuddyVersion);
    }

    private static void ensureDispatcherIsAppendedToBootstrapClasspath(Instrumentation instrumentation)
            throws ClassNotFoundException, IOException {
        final ClassLoader bootstrapClassloader = ClassLoader.getSystemClassLoader().getParent();
        try {
            bootstrapClassloader.loadClass(DISPATCHER_CLASS_NAME);
            // already injected
        } catch (ClassNotFoundException e) {
            final JarFile jarfile = new JarFile(createTempDispatcherJar());
            instrumentation.appendToBootstrapClassLoaderSearch(jarfile);
            bootstrapClassloader.loadClass(DISPATCHER_CLASS_NAME);
        }
    }

    private static File createTempDispatcherJar() throws IOException {
        final InputStream input = AgentAttacher.class.getClassLoader()
                .getResourceAsStream("stagemonitor-dispatcher.jar.gradlePleaseDontExtract");
        if (input == null) {
            throw new IllegalStateException("If you see this exception inside you IDE, you have to execute gradle " +
                    "processResources before running the tests.");
        }
        final File tempDispatcherJar = File.createTempFile("stagemonitor-dispatcher", ".jar");
        tempDispatcherJar.deleteOnExit();
        IOUtils.copy(input, new FileOutputStream(tempDispatcherJar));
        return tempDispatcherJar;
    }

    private static ClassFileTransformer initByteBuddyClassFileTransformer(AutoEvictingCachingBinaryLocator binaryLocator) {
        AgentBuilder agentBuilder = createAgentBuilder(binaryLocator);
        for (SpTracerByteBuddyTransformer transformer : getStagemonitorByteBuddyTransformers()) {
            agentBuilder = agentBuilder
                    .type(transformer.getMatcher())
                    .transform(transformer.getTransformer());
        }

        final long start = System.currentTimeMillis();
        try {
            return agentBuilder.installOn(instrumentation);
        } finally {
            if (corePlugin.isDebugInstrumentation()) {
                logger.info("Installed agent in {} ms", System.currentTimeMillis() - start);
            }
        }
    }

    private static AgentBuilder createAgentBuilder(AutoEvictingCachingBinaryLocator binaryLocator) {
        final ByteBuddy byteBuddy = new ByteBuddy()
                .with(TypeValidation.of(corePlugin.isDebugInstrumentation()))
                .with(MethodGraph.Compiler.ForDeclaredMethods.INSTANCE);
        return new AgentBuilder.Default(byteBuddy)
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .with(getListener())
                .with(binaryLocator)
                .ignore(any(), timed("classloader", "reflection", isReflectionClassLoader()))
                .or(any(), timed("classloader", "groovy-call-site", classLoaderWithName("org.codehaus.groovy.runtime.callsite.CallSiteClassLoader")))
                .or(any(), new IsIgnoredClassLoaderElementMatcher())
                .or(timed("type", "global-exclude", nameStartsWith("org.aspectj.")
                        .or(nameStartsWith("org.groovy."))
                        .or(nameStartsWith("com.p6spy."))
                        .or(nameStartsWith("net.bytebuddy."))
                        .or(nameStartsWith("org.slf4j.").and(not(nameStartsWith("org.slf4j.impl."))))
                        .or(nameContains("javassist"))
                        .or(nameContains(".asm."))
                        .or(nameStartsWith("org.stagemonitor")
                                .and(not(nameContains("Test")
                                        .or(nameContains("benchmark"))
                                        .or(nameStartsWith("org.stagemonitor.demo")))))
                ))
                .disableClassFormatChanges();
    }

    private static AgentBuilder.Listener getListener() {
        List<AgentBuilder.Listener> listeners = new ArrayList<AgentBuilder.Listener>(2);
        if (corePlugin.isDebugInstrumentation()) {
            listeners.add(new ErrorLoggingListener());
        }
        if (!corePlugin.getExportClassesWithName().isEmpty()) {
            listeners.add(new FileExportingListener(corePlugin.getExportClassesWithName()));
        }
        return new AgentBuilder.Listener.Compound(listeners);
    }

    private static Iterable<SpTracerByteBuddyTransformer> getStagemonitorByteBuddyTransformers() {
        List<SpTracerByteBuddyTransformer> transformers = new ArrayList<SpTracerByteBuddyTransformer>();
        for (SpTracerByteBuddyTransformer transformer : ServiceLoader.load(SpTracerByteBuddyTransformer.class, SpTracer.class.getClassLoader())) {
            try {
                if (transformer.isActive() && !isExcluded(transformer)) {
                    transformers.add(transformer);
                    if (corePlugin.isDebugInstrumentation()) {
                        logger.info("Registering {}", transformer.getClass().getSimpleName());
                    }
                } else if (corePlugin.isDebugInstrumentation()) {
                    logger.info("Excluding {}", transformer.getClass().getSimpleName());
                }
            } catch (NoClassDefFoundError e) {
                logger.warn("NoClassDefFoundError when trying to apply {}. " +
                        "Make sure that optional types are not referenced directly.", transformer, e);
            }
        }
        Collections.sort(transformers, new Comparator<SpTracerByteBuddyTransformer>() {
            @Override
            public int compare(SpTracerByteBuddyTransformer o1, SpTracerByteBuddyTransformer o2) {
                return o1.getOrder() > o2.getOrder() ? 1 : -1;
            }
        });
        return transformers;
    }

    private static boolean isExcluded(SpTracerByteBuddyTransformer transformer) {
        return corePlugin.getExcludedInstrumenters().contains(transformer.getClass().getSimpleName());
    }

    private static class IsIgnoredClassLoaderElementMatcher implements ElementMatcher<ClassLoader> {
        @Override
        public boolean matches(ClassLoader target) {
            return hashCodesOfClassLoadersToIgnore.contains(ClassUtils.getIdentityString(target));
        }
    }

}
