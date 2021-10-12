package com.sptracer.util;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class UrlConnectionUtils {
    public static URLConnection openUrlConnectionThreadSafely(URL url) throws IOException {
        GlobalLocks.JUL_INIT_LOCK.lock();
        try {
            return url.openConnection();
        } finally {
            GlobalLocks.JUL_INIT_LOCK.unlock();
        }
    }
}
