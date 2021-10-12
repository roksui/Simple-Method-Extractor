package com.sptracer;

import com.dslplatform.json.JsonWriter;
import com.sptracer.error.ErrorCapture;
import com.sptracer.impl.Span;

import java.io.Closeable;
import java.util.concurrent.Future;

public interface Reporter extends Closeable {

    void start();

    void report(com.sptracer.impl.Transaction transaction);

    void report(Span span);

    void report(ErrorCapture error);

    void report(JsonWriter jsonWriter);

    long getDropped();

    long getReported();

    Future<Void> flush();

    @Override
    void close();
}
