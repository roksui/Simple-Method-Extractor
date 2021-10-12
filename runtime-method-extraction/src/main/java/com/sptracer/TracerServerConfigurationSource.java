package com.sptracer;

import com.sptracer.configuration.ConfigurationOption;
import com.sptracer.configuration.ConfigurationRegistry;
import com.sptracer.configuration.CoreConfiguration;
import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.MapConverter;
import com.sptracer.configuration.source.AbstractConfigurationSource;
import com.sptracer.impl.SpTracerImpl;
import com.sptracer.util.ExecutorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TracerServerConfigurationSource extends AbstractConfigurationSource implements LifecycleListener {
    private static final int SC_OK = 200;
    private static final int SC_NOT_MODIFIED = 304;
    private static final int SC_FORBIDDEN = 403;
    private static final int SC_NOT_FOUND = 404;
    private static final int SC_SERVICE_UNAVAILABLE = 503;

    private static final int DEFAULT_POLL_DELAY_SEC = (int) TimeUnit.MINUTES.toSeconds(5);
    private static final Pattern MAX_AGE = Pattern.compile("max-age\\s*=\\s*(\\d+)");
    private final Logger logger;
    private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>());
    private final byte[] buffer = new byte[4096];
    private final PayloadSerializer payloadSerializer;
    private final TracerServerClient tracerServerClient;
    @Nullable
    private String etag;
    private volatile Map<String, String> config = Collections.emptyMap();
    @Nullable
    private volatile ThreadPoolExecutor threadPool;

    public TracerServerConfigurationSource(PayloadSerializer payloadSerializer, TracerServerClient tracerServerClient) {
        this(payloadSerializer, tracerServerClient, LoggerFactory.getLogger(TracerServerConfigurationSource.class));
    }

    TracerServerConfigurationSource(PayloadSerializer payloadSerializer, TracerServerClient tracerServerClient, Logger logger) {
        this.payloadSerializer = payloadSerializer;
        this.tracerServerClient = tracerServerClient;
        this.logger = logger;
    }

    @Nullable
    static Integer parseMaxAge(@Nullable String cacheControlHeader) {
        if (cacheControlHeader == null) {
            return null;
        }

        Matcher matcher = MAX_AGE.matcher(cacheControlHeader);
        if (!matcher.find()) {
            return null;
        }
        return Integer.parseInt(matcher.group(1));
    }


    @Override
    public void reload() {
    }

    @Override
    public void init(SpTracerImpl tracer) throws Exception {
        // noop
    }

    @Override
    public void start(final SpTracerImpl tracer) {
        threadPool = ExecutorUtils.createSingleThreadDaemonPool("remote-config-poller", 1);
        threadPool.execute(new Runnable() {
            @Override
            public void run() {
                pollConfig(tracer.getConfigurationRegistry());
            }
        });
    }

    /**
     * Continuously polls the APM Server's remote configuration endpoint
     *
     * @param configurationRegistry the configuration registry which will be asked to
     *                              {@link ConfigurationRegistry#reloadDynamicConfigurationOptions()}
     *                              after successfully fetching the configuration
     */
    private void pollConfig(ConfigurationRegistry configurationRegistry) {
        while (!Thread.currentThread().isInterrupted()) {
            String cacheControlHeader = fetchConfig(configurationRegistry);
            // it doesn't make sense to poll more frequently than the max-age
            Integer pollDelaySec = parseMaxAge(cacheControlHeader);
            if (pollDelaySec == null) {
                pollDelaySec = DEFAULT_POLL_DELAY_SEC;
            }
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("Scheduling next remote configuration reload in {}s", pollDelaySec);
                }
                TimeUnit.SECONDS.sleep(pollDelaySec);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nullable
    String fetchConfig(final ConfigurationRegistry configurationRegistry) {
        if (!configurationRegistry.getConfig(CoreConfiguration.class).isCentralConfigEnabled()) {
            logger.debug("Remote configuration is disabled");
            return null;
        }
        try {
            payloadSerializer.blockUntilReady();
            return tracerServerClient.execute("/config/v1/agents", new TracerServerClient.ConnectionHandler<String>() {
                @Override
                public String withConnection(HttpURLConnection connection) throws IOException {
                    try {
                        return tryFetchConfig(configurationRegistry, connection);
                    } catch (PayloadSerializer.UninitializedException e) {
                        throw new IOException("Cannot fetch configuration from APM Server, serializer not initialized yet", e);
                    }
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    private String tryFetchConfig(ConfigurationRegistry configurationRegistry, HttpURLConnection connection) throws IOException, PayloadSerializer.UninitializedException {
        if (logger.isDebugEnabled()) {
            logger.debug("Reloading configuration from APM Server {}", connection.getURL());
        }
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestMethod("POST");
        connection.setDoOutput(true);
        if (etag != null) {
            connection.setRequestProperty("If-None-Match", etag);
        }
        payloadSerializer.setOutputStream(connection.getOutputStream());
        payloadSerializer.appendMetadataToStream();
        payloadSerializer.fullFlush();
        etag = connection.getHeaderField("ETag");

        final int status = connection.getResponseCode();
        switch (status) {
            case SC_OK:
                InputStream is = connection.getInputStream();
                final JsonReader<Object> reader = dslJson.newReader(is, buffer);
                reader.startObject();
                config = MapConverter.deserialize(reader);
                configurationRegistry.reloadDynamicConfigurationOptions();
                logger.info("Received new configuration from APM Server: {}", config);
                for (Map.Entry<String, String> entry : config.entrySet()) {
                    ConfigurationOption<?> conf = configurationRegistry.getConfigurationOptionByKey(entry.getKey());
                    if (conf == null) {
                        logger.warn("Received unknown remote configuration key {}", entry.getKey());
                    } else if (!conf.isDynamic()) {
                        logger.warn("Can't apply remote configuration {} as this option is not dynamic (aka. reloadable)", entry.getKey());
                    }
                }
                break;
            case SC_NOT_MODIFIED:
                logger.debug("Configuration did not change");
                break;
            case SC_NOT_FOUND:
                logger.debug("This APM Server does not support central configuration. Update to APM Server 7.3+");
                break;
            case SC_FORBIDDEN:
                logger.debug("Central configuration is disabled. Set kibana.enabled: true in your APM Server configuration.");
                break;
            case SC_SERVICE_UNAVAILABLE:
                throw new IllegalStateException("Remote configuration is not available. Check the connection between APM Server and Kibana.");
            default:
                throw new IllegalStateException("Unexpected status " + status + " while fetching configuration");
        }
        return connection.getHeaderField("Cache-Control");
    }

    @Override
    public String getValue(String key) {
        return config.get(key);
    }

    @Override
    public String getName() {
        return "APM Server";
    }

    @Override
    public void pause() {
        // Keep polling for remote config changes, in case the user wants to resume a paused agent or change the stress
        // monitoring configurations.
    }

    @Override
    public void resume() {
    }

    @Override
    public void stop() {
        if (this.threadPool != null) {
            this.threadPool.shutdownNow();
        }
    }
}
