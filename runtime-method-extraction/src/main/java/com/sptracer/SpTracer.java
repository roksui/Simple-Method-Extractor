package com.sptracer;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.sptracer.configuration.ConfigurationLogger;
import com.sptracer.configuration.ConfigurationRegistry;
import com.sptracer.configuration.source.ConfigurationSource;
import com.sptracer.metrics.Metric2Registry;
import com.sptracer.metrics.health.ImmediateResult;
import com.sptracer.metrics.health.OverridableHealthCheckRegistry;
import com.sptracer.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SpTracer {

    private static Logger logger = LoggerFactory.getLogger(SpTracer.class);
    private static ConfigurationRegistry configuration;
    private static boolean initialized;
    private static boolean started;
    private static boolean disabled;
    private static MeasurementSession measurementSession = new MeasurementSession(null, null, null);
    private static List<String> pathsOfWidgetMetricTabPlugins = Collections.emptyList();
    private static List<String> pathsOfWidgetTabPlugins = Collections.emptyList();
    private static Iterable<SpTracerPlugin> plugins;
    private static List<Runnable> onShutdownActions = new CopyOnWriteArrayList<Runnable>();
    private static Metric2Registry metric2Registry = new Metric2Registry(SharedMetricRegistries.getOrCreate("stagemonitor"));
    private static HealthCheckRegistry healthCheckRegistry = new OverridableHealthCheckRegistry();

    private SpTracer() {
    }

    static {
        reloadPluginsAndConfiguration();
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        try {
            reset();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            try {
                // something bad happened, try to shut down
                SpTracer.shutDown();
            } catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    private static void startMonitoring(MeasurementSession measurementSession) {
        if (!getPlugin(CorePlugin.class).getSpTracerActive()) {
            logger.info("stagemonitor is deactivated");
            disabled = true;
        }
        if (started || disabled) {
            return;
        }
        SpTracer.measurementSession = measurementSession;
        doStartMonitoring();
    }

    private static void doStartMonitoring() {
        if (started) {
            return;
        }
        if (measurementSession.isInitialized()) {
            logger.info("Measurement Session is initialized: " + measurementSession);
            try {
                start();
            } catch (RuntimeException e) {
                logger.warn("Error while trying to start monitoring. (this exception is ignored)", e);
            }
        } else {
            logger.debug("Measurement Session is not initialized: {}", measurementSession);
            logger.debug("make sure the properties 'stagemonitor.instanceName' and 'stagemonitor.applicationName' " +
                    "are set and stagemonitor.properties is available in the classpath");
        }
    }

    private static void start() {
        initializePlugins();
        started = true;
        // don't register a shutdown hook for web applications as this causes a memory leak
        if (ClassUtils.isNotPresent("javax.servlet.Servlet")) {
            // in case the application does not directly call shutDown
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                public void run() {
                    shutDown();
                }
            }));
        }
    }

    private static void logStatus() {
        logger.info("# stagemonitor status");
        logger.info("System information: {}", getJvmAndOsVersionString());
        for (Map.Entry<String, HealthCheck.Result> entry : healthCheckRegistry.runHealthChecks().entrySet()) {
            String status = entry.getValue().isHealthy() ? "OK  " : "FAIL";
            String message = entry.getValue().getMessage() == null ? "" : "(" + entry.getValue().getMessage() + ")";
            final String checkName = entry.getKey();
            logger.info("{} - {} {}", status, checkName, message);
            final Throwable error = entry.getValue().getError();
            if (error != null) {
                logger.warn("Exception thrown while initializing plugin", error);
            }
        }
    }

    private static String getJvmAndOsVersionString() {
        return "Java " + System.getProperty("java.version") + " (" + System.getProperty("java.vendor") + ") " +
                System.getProperty("os.name") + " " + System.getProperty("os.version");
    }

    private static void initializePlugins() {
        final CorePlugin corePlugin = getPlugin(CorePlugin.class);
        final Collection<String> disabledPlugins = corePlugin.getDisabledPlugins();
        pathsOfWidgetMetricTabPlugins = new CopyOnWriteArrayList<String>();
        pathsOfWidgetTabPlugins = new CopyOnWriteArrayList<String>();

        initializePluginsInOrder(disabledPlugins, plugins);
    }

    static void initializePluginsInOrder(Collection<String> disabledPlugins, Iterable<SpTracerPlugin> plugins) {
        Set<Class<? extends SpTracerPlugin>> alreadyInitialized = new HashSet<Class<? extends SpTracerPlugin>>();
        Set<SpTracerPlugin> notYetInitialized = getPluginsToInit(disabledPlugins, plugins);
        while (!notYetInitialized.isEmpty()) {
            int countNotYetInitialized = notYetInitialized.size();
            // try to init plugins which are
            for (Iterator<SpTracerPlugin> iterator = notYetInitialized.iterator(); iterator.hasNext(); ) {
                SpTracerPlugin stagemonitorPlugin = iterator.next();
                {
                    final List<Class<? extends SpTracerPlugin>> dependencies = stagemonitorPlugin.dependsOn();
                    if (dependencies.isEmpty() || alreadyInitialized.containsAll(dependencies)) {
                        initializePlugin(stagemonitorPlugin);
                        iterator.remove();
                        alreadyInitialized.add(stagemonitorPlugin.getClass());
                    }
                }
            }
            if (countNotYetInitialized == notYetInitialized.size()) {
                // no plugins could be initialized in this try. this probably means there is a cyclic dependency
                throw new IllegalStateException("Cyclic dependencies detected: " + notYetInitialized);
            }
        }
    }

    private static Set<SpTracerPlugin> getPluginsToInit(Collection<String> disabledPlugins, Iterable<SpTracerPlugin> plugins) {
        Set<SpTracerPlugin> notYetInitialized = new HashSet<SpTracerPlugin>();
        for (SpTracerPlugin stagemonitorPlugin : plugins) {
            final String pluginName = stagemonitorPlugin.getClass().getSimpleName();
            if (disabledPlugins.contains(pluginName)) {
                logger.info("Not initializing disabled plugin {}", pluginName);
                healthCheckRegistry.register(pluginName, ImmediateResult.of(HealthCheck.Result.unhealthy("disabled via configuration")));
            } else {
                notYetInitialized.add(stagemonitorPlugin);
            }
        }
        return notYetInitialized;
    }

    private static void initializePlugin(final SpTracerPlugin stagemonitorPlugin) {
        final String pluginName = stagemonitorPlugin.getClass().getSimpleName();
        try {
            stagemonitorPlugin.initializePlugin(new SpTracerPlugin.InitArguments(metric2Registry, getConfiguration(), measurementSession, healthCheckRegistry));
            stagemonitorPlugin.initialized = true;
            for (Runnable onInitCallback : stagemonitorPlugin.onInitCallbacks) {
                onInitCallback.run();
            }
            stagemonitorPlugin.registerWidgetTabPlugins(new SpTracerPlugin.WidgetTabPluginsRegistry(pathsOfWidgetTabPlugins));
            stagemonitorPlugin.registerWidgetMetricTabPlugins(new SpTracerPlugin.WidgetMetricTabPluginsRegistry(pathsOfWidgetMetricTabPlugins));
            healthCheckRegistry.register(pluginName, ImmediateResult.of(HealthCheck.Result.healthy("version " + stagemonitorPlugin.getVersion())));
        } catch (final Exception e) {
            healthCheckRegistry.register(pluginName, ImmediateResult.of(HealthCheck.Result.unhealthy(e)));
            logger.warn("Error while initializing plugin " + pluginName + " (this exception is ignored)", e);
        }
    }

    public static synchronized void shutDown() {
        if (measurementSession.getEndTimestamp() != null) {
            // shutDown has already been called
            return;
        }
        logger.info("Shutting down stagemonitor");
        measurementSession.setEndTimestamp(System.currentTimeMillis());
        for (Runnable onShutdownAction : onShutdownActions) {
            try {
                onShutdownAction.run();
            } catch (RuntimeException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        for (SpTracerPlugin plugin : plugins) {
            try {
                plugin.onShutDown();
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        configuration.close();
    }

    /**
     * @deprecated use {@link #getMetric2Registry()}
     */
    @Deprecated
    public static MetricRegistry getMetricRegistry() {
        return metric2Registry.getMetricRegistry();
    }

    public static Metric2Registry getMetric2Registry() {
        return metric2Registry;
    }

    public static HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    public static ConfigurationRegistry getConfiguration() {
        return configuration;
    }

    public static <T extends SpTracerPlugin> T getPlugin(Class<T> plugin) {
        return configuration.getConfig(plugin);
    }

    /**
     * @deprecated use {@link #getPlugin(Class)}
     */
    @Deprecated
    public static <T extends SpTracerPlugin> T getConfiguration(Class<T> plugin) {
        return getPlugin(plugin);
    }

    static void setConfiguration(ConfigurationRegistry configuration) {
        SpTracer.configuration = configuration;
    }

    public static MeasurementSession getMeasurementSession() {
        return measurementSession;
    }

    public static boolean isStarted() {
        return started;
    }

    static boolean isDisabled() {
        return disabled;
    }

    static void setLogger(Logger logger) {
        SpTracer.logger = logger;
    }

    public static List<String> getPathsOfWidgetTabPlugins() {
        return Collections.unmodifiableList(pathsOfWidgetTabPlugins);
    }

    public static List<String> getPathsOfWidgetMetricTabPlugins() {
        return Collections.unmodifiableList(pathsOfWidgetMetricTabPlugins);
    }

    /**
     * Should only be used outside of this class by the internal unit tests
     */
    @Deprecated
    public static void reset() {
        reset(null);
    }

    /**
     * Should only be used outside of this class by the internal unit tests
     */
    @Deprecated
    public static void reset(MeasurementSession measurementSession) {
        started = false;
        disabled = false;
        if (configuration == null) {
            reloadPluginsAndConfiguration();
        }
        if (measurementSession == null) {
            CorePlugin corePlugin = getPlugin(CorePlugin.class);
            measurementSession = new MeasurementSession(corePlugin.getApplicationName(),
                    corePlugin.getHostName(), corePlugin.getInstanceName());
        }
        onShutdownActions.add(AgentAttacher.performRuntimeAttachment());
        startMonitoring(measurementSession);
        healthCheckRegistry.register("Startup", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                if (started) {
                    return Result.healthy();
                } else {
                    return Result.unhealthy("stagemonitor is not started");
                }
            }
        });
        logStatus();
        new ConfigurationLogger().logConfiguration(configuration);
    }

    private static void reloadPluginsAndConfiguration() {
        List<ConfigurationSource> configurationSources = new ArrayList<ConfigurationSource>();
        for (SpTracerConfigurationSourceInitializer initializer : ServiceLoader.load(SpTracerConfigurationSourceInitializer.class, SpTracer.class.getClassLoader())) {
            initializer.modifyConfigurationSources(new SpTracerConfigurationSourceInitializer.ModifyArguments(configurationSources));
        }
        configurationSources.remove(null);

        plugins = ServiceLoader.load(SpTracerPlugin.class, SpTracer.class.getClassLoader());
        configuration = ConfigurationRegistry.builder()
                .optionProviders(plugins)
                .configSources(configurationSources)
                .build();

        try {
            for (SpTracerConfigurationSourceInitializer initializer : ServiceLoader.load(SpTracerConfigurationSourceInitializer.class, SpTracer.class.getClassLoader())) {
                initializer.onConfigurationInitialized(new SpTracerConfigurationSourceInitializer.ConfigInitializedArguments(configuration));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("Stagemonitor will be deactivated!");
            disabled = true;
        }
    }
}
