package com.sptracer.util;

import com.sptracer.SpTracerAgent;
import com.sptracer.WeakMap;
import com.sptracer.configuration.source.PropertyFileConfigurationSource;
import com.sptracer.weakconcurrent.WeakConcurrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.jar.JarInputStream;

public final class VersionUtils {

    private static final WeakMap<Class<?>, String> versionsCache = WeakConcurrent.buildMap();
    private static final String UNKNOWN_VERSION = "UNKNOWN_VERSION";
    @Nullable
    private static final String AGENT_VERSION;

    static {
        String version = getVersion(VersionUtils.class, "co.elastic.apm", "elastic-apm-agent");
        if (version != null && version.endsWith("SNAPSHOT")) {
            String gitRev = getManifestEntry(SpTracerAgent.getAgentJarFile(), "SCM-Revision");
            if (gitRev != null) {
                version = version + "." + gitRev;
            }
        }
        AGENT_VERSION = version;
    }

    private VersionUtils() {
    }

    @Nullable
    public static String getAgentVersion() {
        return AGENT_VERSION;
    }

    @Nullable
    public static String getVersion(Class<?> clazz, String groupId, String artifactId) {
        String version = versionsCache.get(clazz);
        if (version != null) {
            return version != UNKNOWN_VERSION ? version : null;
        }
        version = getVersionFromPomProperties(clazz, groupId, artifactId);
        if (version == null) {
            version = getVersionFromPackage(clazz);
        }
        versionsCache.put(clazz, version != null ? version : UNKNOWN_VERSION);
        return version;
    }

    @Nullable
    static String getVersionFromPackage(Class<?> clazz) {
        Package pkg = clazz.getPackage();
        if (pkg != null) {
            return pkg.getImplementationVersion();
        }
        return null;
    }

    @Nullable
    static String getVersionFromPomProperties(Class<?> clazz, String groupId, String artifactId) {
        final String classpathLocation = "/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";
        final Properties pomProperties = getFromClasspath(classpathLocation, clazz);
        if (pomProperties != null) {
            return pomProperties.getProperty("version");
        }
        return null;
    }

    @Nullable
    private static Properties getFromClasspath(String classpathLocation, Class<?> clazz) {
        final Properties props = new Properties();
        try (InputStream resourceStream = clazz.getResourceAsStream(classpathLocation)) {
            if (resourceStream != null) {
                props.load(resourceStream);
                return props;
            }
        } catch (IOException ignore) {
        }
        return null;
    }

    @Nullable
    public static String getManifestEntry(@Nullable File jarFile, String manifestAttribute) {
        if (jarFile == null) {
            return null;
        }
        try (JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarFile))) {
            return jarInputStream.getManifest().getMainAttributes().getValue(manifestAttribute);
        } catch (IOException e) {
            return null;
        }
    }

}
