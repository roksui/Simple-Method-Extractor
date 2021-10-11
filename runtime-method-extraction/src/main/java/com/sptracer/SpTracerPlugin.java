package com.sptracer;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.sptracer.configuration.ConfigurationOptionProvider;
import com.sptracer.configuration.ConfigurationRegistry;
import com.sptracer.configuration.source.PropertyFileConfigurationSource;
import com.sptracer.metrics.Metric2Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class SpTracerPlugin extends ConfigurationOptionProvider implements SpTracerSPI {

    private static final Logger logger = LoggerFactory.getLogger(SpTracerPlugin.class);

    volatile boolean initialized;
    List<Runnable> onInitCallbacks = new CopyOnWriteArrayList<Runnable>();
    private final String version;

    protected SpTracerPlugin() {
        final Properties properties = PropertyFileConfigurationSource.getFromClasspath("org/stagemonitor/version.properties", getClass().getClassLoader());
        if (properties != null) {
            version = properties.getProperty("version");
        } else {
            version = null;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void initializePlugin(InitArguments initArguments) throws Exception {
    }

    /**
     * This method is called when stagemonitor shuts down.
     * The shutdown is triggered by {@code org.stagemonitor.web.servlet.filter.HttpRequestMonitorFilter#destroy()},
     * for example. In non-servlet applications this method has to be called manually.
     * <p>
     * Note that this method will be called even if {@link #initializePlugin(InitArguments)} has not been called.
     */
    public void onShutDown() throws Exception {
    }

    /**
     * StagemonitorPlugins can add additional tabs to the in browser widget.
     * A tab plugin consists of a javascript file and a html file.
     * <p>
     * The files should be placed under
     * <code>src/main/resources/stagemonitor/static/some/sub/folder/myPlugin[.js|.html]</code>
     * if you return the path <code>/stagemonitor/static/some/sub/folder/myPlugin</code>
     * <p>
     * The FileServlet serves all files under /src/main/resources/stagemonitor/static and
     * src/main/resources/stagemonitor/public/static
     */
    public void registerWidgetTabPlugins(WidgetTabPluginsRegistry widgetTabPluginsRegistry) {
    }

    /**
     * StagemonitorPlugins can extend the metrics tab in the in browser widget.
     * A widget metrics tab plugin consists of a javascript file and a html file.
     * <p>
     * The files should be placed under
     * <code>/src/main/resources/stagemonitor/static/some/sub/folder/{pluginId}[.js|.html]</code>
     * <p>
     * The FileServlet serves all files under /src/main/resources/stagemonitor/static and
     * /src/main/resources/stagemonitor/public/static
     * <p>
     * For documentation and a example of how a plugin should look like, see
     * <code>stagemonitor-ehcache/src/main/resources/stagemonitor/static/tabs/metrics</code> and
     * <code>stagemonitor-jvm/src/main/resources/stagemonitor/static/tabs/metrics</code>
     */
    public void registerWidgetMetricTabPlugins(WidgetMetricTabPluginsRegistry widgetMetricTabPluginsRegistry) {
    }

    /**
     * If this plugin is initialized, the callback is executed immediately. Otherwise, it is executed after the plugin
     * has been initialized.
     *
     * @param onInitCallback the callback which is not executed in a different thread
     */
    public void onInit(Runnable onInitCallback) {
        if (initialized) {
            onInitCallback.run();
        } else {
            onInitCallbacks.add(onInitCallback);
        }
    }

    public String getVersion() {
        return version;
    }

    /**
     * Plugins can define dependencies on other plugins.
     * <p>
     * This makes sure that the dependant plugins are initialized first.
     *
     * @return the plugins this plugin depends on
     */
    public List<Class<? extends SpTracerPlugin>> dependsOn() {
        return Collections.<Class<? extends SpTracerPlugin>>singletonList(CorePlugin.class);
    }

    public static class InitArguments {
        private final Metric2Registry metricRegistry;
        private final ConfigurationRegistry configuration;
        private final MeasurementSession measurementSession;
        private final HealthCheckRegistry healthCheckRegistry;

        public InitArguments(Metric2Registry metricRegistry, ConfigurationRegistry configuration, MeasurementSession measurementSession, HealthCheckRegistry healthCheckRegistry) {
            this.metricRegistry = metricRegistry;
            this.configuration = configuration;
            this.measurementSession = measurementSession;
            this.healthCheckRegistry = healthCheckRegistry;
        }

        public Metric2Registry getMetricRegistry() {
            return metricRegistry;
        }

        public ConfigurationRegistry getConfiguration() {
            return configuration;
        }

        public <T extends SpTracerPlugin> T getPlugin(Class<T> pluginClass) {
            final T plugin = configuration.getConfig(pluginClass);
            if (!plugin.isInitialized()) {
                logger.warn("The plugin " + plugin.getClass().getSimpleName() + " has not been initialized yet. " +
                        "You should define a dependency via StagemonitorPlugin#dependsOn to " + getClass().getSimpleName());
            }
            return plugin;
        }

        public MeasurementSession getMeasurementSession() {
            return measurementSession;
        }

        public HealthCheckRegistry getHealthCheckRegistry() {
            return healthCheckRegistry;
        }
    }

    public static class WidgetTabPluginsRegistry {
        private final List<String> pathsOfWidgetTabPlugins;

        WidgetTabPluginsRegistry(List<String> pathsOfWidgetTabPlugins) {
            this.pathsOfWidgetTabPlugins = pathsOfWidgetTabPlugins;
        }

        public void addWidgetTabPlugin(String pathOfWidgetTabPlugin) {
            pathsOfWidgetTabPlugins.add(pathOfWidgetTabPlugin);
        }
    }

    public static class WidgetMetricTabPluginsRegistry {
        private final List<String> pathsOfWidgetMetricTabPlugins;

        WidgetMetricTabPluginsRegistry(List<String> pathsOfWidgetMetricTabPlugins) {
            this.pathsOfWidgetMetricTabPlugins = pathsOfWidgetMetricTabPlugins;
        }

        public void addWidgetMetricTabPlugin(String pathOfWidgetMetricTabPlugin) {
            pathsOfWidgetMetricTabPlugins.add(pathOfWidgetMetricTabPlugin);
        }
    }
}
