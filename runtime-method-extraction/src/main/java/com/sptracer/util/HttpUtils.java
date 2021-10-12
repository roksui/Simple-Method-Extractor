package com.sptracer.util;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

public class HttpUtils {

    private HttpUtils() {
    }

    /**
     * Reads the steam and converts the contents to a string, without closing stream.
     *
     * @param inputStream the input stream
     * @return the content of the stream as a string
     */
    public static String readToString(final InputStream inputStream) throws IOException {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        final StringBuilder bodyString = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            bodyString.append(line);
        }
        return bodyString.toString();
    }

    /**
     * In order to be able to reuse the underlying TCP connections,
     * the input stream must be consumed and closed
     * see also https://docs.oracle.com/javase/8/docs/technotes/guides/net/http-keepalive.html
     *
     * @param connection the connection
     */
    public static void consumeAndClose(@Nullable HttpURLConnection connection) {
        if (connection != null) {
            IOUtils.consumeAndClose(connection.getErrorStream());
            try {
                IOUtils.consumeAndClose(connection.getInputStream());
            } catch (IOException ignored) {
                // silently ignored
            }
        }
    }
}
