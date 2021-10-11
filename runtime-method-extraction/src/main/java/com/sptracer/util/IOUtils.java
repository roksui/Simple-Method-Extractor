package com.sptracer.util;

import com.sptracer.configuration.source.PropertyFileConfigurationSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;

public class IOUtils {

    private static final int EOF = -1;
    private static final int BUFFER_SIZE = 4096;
    private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

    private IOUtils() {
    }

    public static void copy(InputStream input, OutputStream output) throws IOException {
        int n;
        final byte[] buffer = new byte[BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
        }
    }

    public static void write(String str, OutputStream out) throws IOException {
        out.write(str.getBytes());
    }

    public static String toString(InputStream input) throws IOException {
        if (input == null) {
            return null;
        }
        final InputStreamReader inputStreamReader = new InputStreamReader(input);
        final StringBuilder stringBuilder = new StringBuilder();
        final char[] buffer = new char[BUFFER_SIZE];
        int n = 0;
        while (EOF != (n = inputStreamReader.read(buffer))) {
            stringBuilder.append(buffer, 0, n);
        }
        return stringBuilder.toString();
    }

    public static void closeQuietly(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static void consumeAndClose(InputStream is) {
        if (is == null) {
            return;
        }
        try {
            while (is.read() != EOF) {}
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        } finally {
            closeQuietly(is);
        }
    }

    public static InputStream getResourceAsStream(String name) {
        return IOUtils.class.getClassLoader().getResourceAsStream(name);
    }

    public static String getResourceAsString(String name) {
        try {
            return toString(getResourceAsStream(name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] readToBytes(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return new byte[0];
        }
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(inputStream, output);
        return output.toByteArray();
    }

    public static File getFile(String classPathLocation) throws IOException, URISyntaxException {
        URL resource = PropertyFileConfigurationSource.class.getClassLoader().getResource(classPathLocation);
        if (resource == null) {
            resource = new URL("file://" + classPathLocation);
        }
        if (!"file".equals(resource.getProtocol())) {
            throw new IOException("Saving to property files inside a war, ear or jar is not possible.");
        }
        return new File(resource.toURI());
    }
}