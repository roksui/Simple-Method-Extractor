package com.sptracer;

import java.io.Closeable;

public class ClosableLifecycleListenerAdapter extends AbstractLifecycleListener {

    private final Closeable closeable;

    public static LifecycleListener of(Closeable closeable) {
        return new ClosableLifecycleListenerAdapter(closeable);
    }

    private ClosableLifecycleListenerAdapter(Closeable closeable) {
        this.closeable = closeable;
    }

    @Override
    public void stop() throws Exception {
        closeable.close();
    }
}
