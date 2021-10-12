package com.sptracer;

public interface Scope extends AutoCloseable {

    @Override
    void close();

    enum NoopScope implements Scope {
        INSTANCE;

        @Override
        public void close() {
            // noop
        }
    }
}
