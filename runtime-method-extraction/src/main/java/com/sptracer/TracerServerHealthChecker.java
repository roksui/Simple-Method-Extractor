package com.sptracer;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.MapConverter;
import com.dslplatform.json.Nullable;
import com.dslplatform.json.ObjectConverter;
import com.sptracer.util.ExecutorUtils;
import com.sptracer.util.HttpUtils;
import com.sptracer.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TracerServerHealthChecker implements Callable<Version> {
    private static final Logger logger = LoggerFactory.getLogger(TracerServerHealthChecker.class);

    private final TracerServerClient tracerServerClient;
    private final DslJson<Object> dslJson = new DslJson<>(new DslJson.Settings<>());

    public TracerServerHealthChecker(TracerServerClient apmServerClient) {
        this.tracerServerClient = apmServerClient;
    }

    public Future<Version> checkHealthAndGetMinVersion() {
        ThreadPoolExecutor pool = ExecutorUtils.createSingleThreadDaemonPool("server-healthcheck", 1);
        try {
            return pool.submit(this);
        } finally {
            pool.shutdown();
        }
    }

    @Nullable
    @Override
    public Version call() {
        List<Version> versions = tracerServerClient.executeForAllUrls("/", new TracerServerClient.ConnectionHandler<Version>() {
            @Override
            public Version withConnection(HttpURLConnection connection) {
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Starting healthcheck to {}", connection.getURL());
                    }

                    final int status = connection.getResponseCode();
                    if (status >= 300) {
                        if (status == 404) {
                            throw new IllegalStateException("It seems like you are using a version of the APM Server which is not compatible with this agent. " +
                                    "Please use APM Server 6.5.0 or newer.");
                        } else {
                            throw new IllegalStateException("Server returned status " + status);
                        }
                    } else {
                        try {
                            // prints out the version info of the APM Server
                            String body = HttpUtils.readToString(connection.getInputStream());
                            logger.info("Elastic APM server is available: {}", body);
                            JsonReader<Object> reader = dslJson.newReader(body.getBytes(UTF_8));
                            reader.startObject();
                            String versionString;
                            try {
                                // newer APM server versions contain a flat map at the JSON root
                                versionString = MapConverter.deserialize(reader).get("version");
                            } catch (Exception e) {
                                // 6.x APM server versions' JSON has a root object of which value is the same map
                                reader = dslJson.newReader(body.getBytes(UTF_8));
                                reader.startObject();
                                Map<String, Object> root = ObjectConverter.deserializeMap(reader);
                                //noinspection unchecked
                                versionString = ((Map<String, String>) root.get("ok")).get("version");
                            }
                            if (logger.isDebugEnabled()) {
                                logger.debug("APM server {} version is: {}", connection.getURL(), versionString);
                            }
                            return Version.of(versionString);
                        } catch (Exception e) {
                            logger.warn("Failed to parse version of APM server {}: {}", connection.getURL(), e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    logger.warn("Elastic APM server {} is not available ({})", connection.getURL(), e.getMessage());
                }
                return null;
            }
        });
        versions.remove(null);
        if (!versions.isEmpty()) {
            return Collections.min(versions);
        }
        return Version.UNKNOWN_VERSION;
    }
}
