package com.sptracer;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.sptracer.configuration.ConfigurationOption;
import com.sptracer.configuration.ConfigurationRegistry;
import com.sptracer.configuration.converter.SetValueConverter;
import com.sptracer.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;


public class CorePlugin extends SpTracerPlugin {

    private static final String CORE_PLUGIN_NAME = "Core";
    public static final String POOLS_QUEUE_CAPACITY_LIMIT_KEY = "stagemonitor.threadPools.queueCapacityLimit";
    private static final String ELASTICSEARCH = "elasticsearch";
    private static final String METRICS_STORE = "metrics-store";

    private static final Logger logger = LoggerFactory.getLogger(CorePlugin.class);

    private final ConfigurationOption<Boolean> spTracerActive = ConfigurationOption.booleanOption()
            .key("stagemonitor.active")
            .dynamic(true)
            .label("Activate stagemonitor")
            .description("If set to `false` stagemonitor will be completely deactivated.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(true);
    private final ConfigurationOption<Boolean> internalMonitoring = ConfigurationOption.booleanOption()
            .key("stagemonitor.internal.monitoring")
            .dynamic(true)
            .label("Internal monitoring")
            .description("If active, stagemonitor will collect internal performance data")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(false);
    private final ConfigurationOption<Integer> reportingIntervalConsole = ConfigurationOption.integerOption()
            .key("stagemonitor.reporting.interval.console")
            .dynamic(false)
            .label("Reporting interval console")
            .description("The amount of time between console reports (in seconds). " +
                    "To deactivate console reports, set this to a value below 1.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(0);
    private final ConfigurationOption<Integer> numberOfShards = ConfigurationOption.integerOption()
            .key("stagemonitor.reporting.elasticsearch.numberOfShards")
            .aliasKeys("stagemonitor.elasticsearch.numberOfShards")
            .dynamic(false)
            .label("Number of ES Shards")
            .description("Sets the number of shards of the Elasticsearch index templates.")
            .tags(METRICS_STORE, ELASTICSEARCH)
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(1);
    private final ConfigurationOption<String> applicationName = ConfigurationOption.stringOption()
            .key("stagemonitor.applicationName")
            .dynamic(false)
            .label("Application name")
            .description("The name of the application.\n" +
                    "It is highly recommended to set this to a short and descriptive name of you application. " +
                    "The dashboards provide a filter for the application name.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .tags("important")
            .buildWithDefault("My Application");
    private final ConfigurationOption<String> instanceName = ConfigurationOption.stringOption()
            .key("stagemonitor.instanceName")
            .dynamic(false)
            .label("Instance name")
            .description("The instance name.\n" +
                    "The instance or stage of your application. For example prod, test, test1, dev. " +
                    "It's important to set this to a useful value because the dashboards provide filters for the instance.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .tags("important")
            .buildWithDefault("My Instance");
    private final ConfigurationOption<String> hostName = ConfigurationOption.stringOption()
            .key("stagemonitor.hostName")
            .dynamic(false)
            .label("Host name")
            .description("The host name.\n" +
                    "If this property is not set, the host name will default to resolving the host name for localhost, " +
                    "if this fails it will be loaded from the environment, either from COMPUTERNAME, HOSTNAME or HOST. " +
                    "The dashboards provide a filter for the host name.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(getNameOfLocalHost());
    private final ConfigurationOption<List<URL>> elasticsearchUrls = ConfigurationOption.urlsOption()
            .key("stagemonitor.reporting.elasticsearch.url")
            .aliasKeys("stagemonitor.elasticsearch.url")
            .dynamic(true)
            .label("Elasticsearch URL")
            .description("A comma separated list of the Elasticsearch URLs that store spans and metrics. " +
                    "If your ES cluster is secured with basic authentication, you can use urls like https://user:password@example.com. " +
                    "The authentication information must be in application/x-www-form-urlencoded format. " +
                    "You can also specify default credentials (in plain text) with stagemonitor.reporting.elasticsearch.username and stagemonitor.reporting.elasticsearch.password."
            )
            .configurationCategory(CORE_PLUGIN_NAME)
            .tags(ELASTICSEARCH)
            .buildWithDefault(Collections.<URL>emptyList());
    private final ConfigurationOption<String> elasticsearchDefaultUsername = ConfigurationOption.stringOption()
            .key("stagemonitor.reporting.elasticsearch.username")
            .dynamic(true)
            .label("Elasticsearch Username")
            .description("The default username for all Elasticsearch URLs defined by stagemonitor.reporting.elasticsearch.url in plain text format.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .tags(ELASTICSEARCH)
            .buildWithDefault("");
    private final ConfigurationOption<String> elasticsearchDefaultPassword = ConfigurationOption.stringOption()
            .key("stagemonitor.reporting.elasticsearch.password")
            .dynamic(true)
            .label("Elasticsearch Password")
            .description("The default password for all Elasticsearch URLs defined by stagemonitor.reporting.elasticsearch.url in plain text format.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .tags(ELASTICSEARCH)
            .sensitive()
            .buildWithDefault("");
    private final ConfigurationOption<Collection<String>> elasticsearchConfigurationSourceProfiles = ConfigurationOption.stringsOption()
            .key("stagemonitor.configuration.elasticsearch.configurationSourceProfiles")
            .aliasKeys("stagemonitor.elasticsearch.configurationSourceProfiles")
            .dynamic(false)
            .label("Elasticsearch configuration source profiles")
            .description("Set configuration profiles of configuration stored in elasticsearch as a centralized configuration source " +
                    "that can be shared between multiple server instances. Set the profiles appropriate to the current " +
                    "environment e.g. `common,prod`, `local`, `test`, ..." +
                    "When you provide multiple profiles, the later ones have precedence over the first ones. " +
                    "The configuration will be stored under " +
                    "`{stagemonitor.reporting.elasticsearch.url}/stagemonitor-configuration/configuration/{configurationSourceProfile}`.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(Collections.<String>emptyList());
    private final ConfigurationOption<Boolean> deactivateStagemonitorIfEsConfigSourceIsDown = ConfigurationOption.booleanOption()
            .key("stagemonitor.configuration.elasticsearch.deactivateStagemonitorIfEsIsDown")
            .aliasKeys("stagemonitor.elasticsearch.configurationSource.deactivateStagemonitorIfEsIsDown")
            .dynamic(false)
            .label("Deactivate stagemonitor if elasticsearch configuration source is down")
            .description("Set to true if stagemonitor should be deactivated if " +
                    "stagemonitor.configuration.elasticsearch.configurationSourceProfiles is set but elasticsearch can't be reached " +
                    "under stagemonitor.reporting.elasticsearch.url. Defaults to true to prevent starting stagemonitor with " +
                    "wrong configuration.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(true);
    private final ConfigurationOption<Collection<MetricName>> excludedMetrics = ConfigurationOption
            .builder(new SetValueConverter<MetricName>(new MetricNameValueConverter()), Collection.class)
            .key("stagemonitor.metrics.excluded.pattern")
            .dynamic(false)
            .label("Excluded metric names")
            .description("A comma separated list of metric names that should not be collected.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(Collections.<MetricName>emptyList());
    private final ConfigurationOption<Collection<String>> disabledPlugins = ConfigurationOption.stringsOption()
            .key("stagemonitor.plugins.disabled")
            .dynamic(false)
            .label("Disabled plugins")
            .description("A comma separated list of plugin names (the simple class name) that should not be active.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(Collections.<String>emptyList());
    private final ConfigurationOption<Integer> reloadConfigurationInterval = ConfigurationOption.integerOption()
            .key("stagemonitor.configuration.reload.interval")
            .dynamic(false)
            .label("Configuration reload interval")
            .description("The interval in seconds a reload of all configuration sources is performed. " +
                    "Set to a value below `1` to deactivate periodic reloading the configuration.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(60);
    private final ConfigurationOption<Collection<String>> excludePackages = ConfigurationOption.stringsOption()
            .key("stagemonitor.instrument.exclude")
            .dynamic(false)
            .label("Excluded packages")
            .description("Exclude packages and their sub-packages from the instrumentation (for example the profiler).")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(Collections.<String>emptySet());
    private final ConfigurationOption<Collection<String>> excludeContaining = ConfigurationOption.stringsOption()
            .key("stagemonitor.instrument.excludeContaining")
            .dynamic(false)
            .label("Exclude containing")
            .description("Exclude classes from the instrumentation (for example from profiling) that contain one of the " +
                    "following strings as part of their class name.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(SetValueConverter.immutableSet("$JaxbAccessor", "$$", "CGLIB", "EnhancerBy", "$Proxy"));
    private final ConfigurationOption<Collection<String>> includePackages = ConfigurationOption.stringsOption()
            .key("stagemonitor.instrument.include")
            .dynamic(false)
            .label("Included packages")
            .description("The packages that should be included for instrumentation. " +
                    "All subpackages of the listed packages are included automatically. " +
                    "This property is required if you want to use the profiler, the @MonitorRequests annotation, the " +
                    "com.codahale.metrics.annotation.* annotations or similar features. " +
                    "You can exclude subpackages of a included package via `stagemonitor.instrument.exclude`. " +
                    "Example: `org.somecompany.package, com.someothercompany`")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(Collections.<String>emptySet());
    private final ConfigurationOption<Boolean> attachAgentAtRuntime = ConfigurationOption.booleanOption()
            .key("stagemonitor.instrument.runtimeAttach")
            .dynamic(false)
            .label("Attach agent at runtime")
            .description("Attaches the agent via the Attach API at runtime and retransforms all currently loaded classes.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(true);
    private final ConfigurationOption<Collection<String>> exportClassesWithName = ConfigurationOption.stringsOption()
            .key("stagemonitor.instrument.exportGeneratedClassesWithName")
            .dynamic(false)
            .label("Export generated classes with name")
            .description("A list of the fully qualified class names which should be exported to the file system after they have been " +
                    "modified by Byte Buddy. This option is useful to debug problems inside the generated class. " +
                    "Classes are exported to a temporary directory. The logs contain the information where the files " +
                    "are stored.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(Collections.<String>emptySet());
    private final ConfigurationOption<Boolean> debugInstrumentation = ConfigurationOption.booleanOption()
            .key("stagemonitor.instrument.debug")
            .dynamic(false)
            .label("Debug instrumentation")
            .description("Set to true to log additional information and warnings during the instrumentation process.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(false);
    private final ConfigurationOption<Collection<String>> excludedInstrumenters = ConfigurationOption.stringsOption()
            .key("stagemonitor.instrument.excludedInstrumenter")
            .dynamic(false)
            .label("Excluded Instrumenters")
            .description("A list of the simple class names of StagemonitorByteBuddyTransformers that should not be applied")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(Collections.<String>emptySet());
    private final ConfigurationOption<Integer> threadPoolQueueCapacityLimit = ConfigurationOption.integerOption()
            .key(POOLS_QUEUE_CAPACITY_LIMIT_KEY)
            .dynamic(false)
            .label("Thread Pool Queue Capacity Limit")
            .description("Sets a limit to the number of pending tasks in the ExecutorServices stagemonitor uses. " +
                    "These are thread pools that are used for example to report spans to elasticsearch. " +
                    "If elasticsearch is unreachable or your application encounters a spike in incoming requests this limit could be reached. " +
                    "It is used to prevent the queue from growing indefinitely. ")
            .configurationCategory(CORE_PLUGIN_NAME)
            .tags("advanced")
            .buildWithDefault(1000);
    private final ConfigurationOption<String> metricsIndexTemplate = ConfigurationOption.stringOption()
            .key("stagemonitor.reporting.elasticsearch.metricsIndexTemplate")
            .dynamic(true)
            .label("ES Metrics Index Template")
            .description("The classpath location of the index template that is used for the stagemonitor-metrics-* indices. " +
                    "By specifying the location to your own template, you can fully customize the index template.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .tags(METRICS_STORE, ELASTICSEARCH)
            .buildWithDefault("stagemonitor-elasticsearch-metrics-index-template.json");
    private final ConfigurationOption<Integer> elasticsearchAvailabilityCheckPeriodSec = ConfigurationOption.integerOption()
            .key("stagemonitor.reporting.elasticsearch.availabilityCheckPeriodSec")
            .aliasKeys("stagemonitor.elasticsearch.availabilityCheckPeriodSec")
            .dynamic(false)
            .label("Elasticsearch availability check period (sec)")
            .description("When set to a value > 0 stagemonitor periodically checks if Elasticsearch is available. " +
                    "When not available, stagemonitor won't try send documents to Elasticsearch which would " +
                    "fail anyway. This reduces heap usage as the documents won't be queued up. " +
                    "It also avoids the logging of warnings when the queue is filled up to the limit (see '" + POOLS_QUEUE_CAPACITY_LIMIT_KEY + "')")
            .configurationCategory(CORE_PLUGIN_NAME)
            .tags("elasticsearch", "advanced")
            .buildWithDefault(5);
    private final ConfigurationOption<List<URL>> remotePropertiesConfigUrls = ConfigurationOption.urlsOption()
            .key("stagemonitor.configuration.remoteproperties.urls")
            .dynamic(false)
            .label("URLs of the remote properties")
            .description("Must be http or https URLs. This can be a single URL or a list of config URLs. " +
                    "The end point should provide a list of properties in a simple line oriented format with key/value pairs. " +
                    "For more information on the format, see java.util.Properties.load(java.io.Reader)). " +
                    "For example of a configuration URL for the petclinic application with the default profile from a" +
                    "Spring Cloud Config server would look like: https://config.server/petclinic-default.properties")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(Collections.<URL>emptyList());
    private final ConfigurationOption<Boolean> deactivateStagemonitorIfRemotePropertyServerIsDown = ConfigurationOption.booleanOption()
            .key("stagemonitor.configuration.remoteproperties.deactivateStagemonitorIfRemotePropertyServerIsDown")
            .dynamic(false)
            .label("Deactivate stagemonitor if the remote properties configuration server is down or can't be reached")
            .description("Set to true if stagemonitor should be deactivated if the config url specified " +
                    "under stagemonitor.reporting.remoteproperties.urls is unavailable. Defaults to true to prevent starting stagemonitor with " +
                    "wrong configuration.")
            .configurationCategory(CORE_PLUGIN_NAME)
            .buildWithDefault(true);

    private List<Closeable> reporters = new CopyOnWriteArrayList<Closeable>();

    private Metric2Registry metricRegistry;
    private AtomicInteger accessesToElasticsearchUrl = new AtomicInteger();
    private HealthCheckRegistry healthCheckRegistry;

    public CorePlugin() {
    }

    @Override
    public void initializePlugin(InitArguments initArguments) {
        this.metricRegistry = initArguments.getMetricRegistry();
        this.healthCheckRegistry = initArguments.getHealthCheckRegistry();
        final Integer reloadInterval = getReloadConfigurationInterval();
        if (reloadInterval > 0) {
            initArguments.getConfiguration().scheduleReloadAtRate(reloadInterval, TimeUnit.SECONDS);
        }

        initArguments.getMetricRegistry().register(MetricName.name("online").build(), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return 1;
            }
        });

        registerReporters(initArguments.getMetricRegistry(), initArguments.getConfiguration(), initArguments.getMeasurementSession());
    }

    @Override
    public List<Class<? extends SpTracerPlugin>> dependsOn() {
        return Collections.emptyList();
    }

    void registerReporters(Metric2Registry metric2Registry, ConfigurationRegistry configuration, MeasurementSession measurementSession) {
        Metric2Filter regexFilter = Metric2Filter.ALL;
        Collection<MetricName> excludedMetricsPatterns = getExcludedMetricsPatterns();
        if (!excludedMetricsPatterns.isEmpty()) {
            regexFilter = MetricNameFilter.excludePatterns(excludedMetricsPatterns);
        }

        Metric2Filter allFilters = new AndMetric2Filter(regexFilter, new MetricsWithCountFilter());
        MetricRegistry legacyMetricRegistry = metric2Registry.getMetricRegistry();

        reportToConsole(metric2Registry, getConsoleReportingInterval(), allFilters);
    }



    private void reportToConsole(Metric2Registry metric2Registry, long reportingInterval, Metric2Filter filter) {
        final SortedTableLogReporter reporter = SortedTableLogReporter.forRegistry(metric2Registry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .filter(filter)
                .build();
        if (reportingInterval > 0) {
            reporter.start(reportingInterval, TimeUnit.SECONDS);
            reporters.add(reporter);
        }
    }

    private void reportToJMX(MetricRegistry metricRegistry) {
        final JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).build();
        reporter.start();
        reporters.add(reporter);
    }

    @Override
    public void onShutDown() {
        for (Closeable reporter : reporters) {
            try {
                reporter.close();
            } catch (IOException e) {
                logger.warn(e.getMessage(), e);
            }
        }

    }

    public MeasurementSession getMeasurementSession() {
        return SpTracer.getMeasurementSession();
    }

    public Metric2Registry getMetricRegistry() {
        return metricRegistry;
    }

    public static String getNameOfLocalHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return getHostNameFromEnv();
        }
    }

    static String getHostNameFromEnv() {
        // try environment properties.
        String host = System.getenv("COMPUTERNAME");
        if (host == null) {
            host = System.getenv("HOSTNAME");
        }
        if (host == null) {
            host = System.getenv("HOST");
        }
        return host;
    }

    public boolean getSpTracerActive() {
        return SpTracer.isDisabled() ? false : spTracerActive.getValue();
    }

    public boolean isInternalMonitoringActive() {
        return internalMonitoring.getValue();
    }

    public long getConsoleReportingInterval() {
        return reportingIntervalConsole.getValue();
    }

    public String getApplicationName() {
        return applicationName.getValue();
    }

    public String getInstanceName() {
        return instanceName.getValue();
    }

    public String getHostName() {
        return hostName.getValue();
    }

    /**
     * Cycles through all provided Elasticsearch URLs and returns one
     *
     * @return One of the provided Elasticsearch URLs
     */
    public URL getElasticsearchUrl() {
        final List<URL> urls = elasticsearchUrls.getValue();
        if (urls.isEmpty()) {
            return null;
        }
        final int index = accessesToElasticsearchUrl.getAndIncrement() % urls.size();
        URL elasticsearchURL = urls.get(index);

        final String defaultUsernameValue = elasticsearchDefaultUsername.getValue();
        final String defaultPasswordValue = elasticsearchDefaultPassword.getValue();

        if (elasticsearchURL.getUserInfo() == null
                && ! defaultUsernameValue.isEmpty()
                && ! defaultPasswordValue.isEmpty()) {

            try {
                String username = URLEncoder.encode(defaultUsernameValue, "UTF-8");
                String password = URLEncoder.encode(defaultPasswordValue, "UTF-8");
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder
                        .append(elasticsearchURL.getProtocol())
                        .append("://")
                        .append(username)
                        .append(":")
                        .append(password)
                        .append("@")
                        .append(elasticsearchURL.getHost())
                        .append(":")
                        .append(elasticsearchURL.getPort())
                        .append(elasticsearchURL.getPath());
                return new URL(stringBuilder.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return elasticsearchURL;
    }

    public Collection<URL> getElasticsearchUrls() {
        return elasticsearchUrls.getValue();
    }

    public String getElasticsearchUrlsWithoutAuthenticationInformation() {
        return elasticsearchUrls.getValueAsSafeString();
    }

    public Collection<String> getElasticsearchConfigurationSourceProfiles() {
        return elasticsearchConfigurationSourceProfiles.getValue();
    }

    public boolean isDeactivateStagemonitorIfEsConfigSourceIsDown() {
        return deactivateStagemonitorIfEsConfigSourceIsDown.getValue();
    }

    public Collection<MetricName> getExcludedMetricsPatterns() {
        return excludedMetrics.getValue();
    }

    public Collection<String> getDisabledPlugins() {
        return disabledPlugins.getValue();
    }

    public Integer getReloadConfigurationInterval() {
        return reloadConfigurationInterval.getValue();
    }

    public Collection<String> getExcludeContaining() {
        return excludeContaining.getValue();
    }

    public Collection<String> getIncludePackages() {
        return includePackages.getValue();
    }

    public Collection<String> getExcludePackages() {
        return excludePackages.getValue();
    }

    public boolean isAttachAgentAtRuntime() {
        return attachAgentAtRuntime.getValue();
    }

    public Collection<String> getExcludedInstrumenters() {
        return excludedInstrumenters.getValue();
    }

    public int getThreadPoolQueueCapacityLimit() {
        return threadPoolQueueCapacityLimit.getValue();
    }

    public boolean isDebugInstrumentation() {
        return debugInstrumentation.getValue();
    }

    public Collection<String> getExportClassesWithName() {
        return exportClassesWithName.getValue();
    }

    public Integer getNumberOfShards() {
        return numberOfShards.getValue();
    }

    public void closeOnShutdown(Closeable closeable) {
        reporters.add(closeable);
    }

    List<Closeable> getReporters() {
        return reporters;
    }

    public List<URL> getRemotePropertiesConfigUrls() {
        return remotePropertiesConfigUrls.getValue();
    }

    public boolean isDeactivateStagemonitorIfRemotePropertyServerIsDown() {
        return deactivateStagemonitorIfRemotePropertyServerIsDown.getValue();
    }

    public String getMetricsIndexTemplate() {
        return metricsIndexTemplate.get();
    }

    public boolean isMetricsIndexTemplateDefaultValue() {
        return metricsIndexTemplate.isDefault();
    }

    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }
}
