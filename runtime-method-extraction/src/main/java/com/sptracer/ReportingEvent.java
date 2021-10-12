package com.sptracer;

import com.dslplatform.json.JsonWriter;
import com.sptracer.error.ErrorCapture;
import com.sptracer.impl.Span;
import com.sptracer.impl.Transaction;

import javax.annotation.Nullable;

import static com.sptracer.ReportingEvent.ReportingEventType.*;

public class ReportingEvent {
    @Nullable
    private com.sptracer.impl.Transaction transaction;
    @Nullable
    private ReportingEventType type;
    @Nullable
    private ErrorCapture error;
    @Nullable
    private com.sptracer.impl.Span span;
    @Nullable
    private JsonWriter jsonWriter;

    public void resetState() {
        this.transaction = null;
        this.type = null;
        this.error = null;
        this.span = null;
        this.jsonWriter = null;
    }

    @Nullable
    public com.sptracer.impl.Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
        this.type = TRANSACTION;
    }

    public void setFlushEvent() {
        this.type = FLUSH;
    }

    @Nullable
    public ReportingEventType getType() {
        return type;
    }

    @Nullable
    public ErrorCapture getError() {
        return error;
    }

    @Nullable
    public Span getSpan() {
        return span;
    }

    public void setError(ErrorCapture error) {
        this.error = error;
        this.type = ReportingEventType.ERROR;
    }

    public void setSpan(com.sptracer.impl.Span span) {
        this.span = span;
        this.type = ReportingEventType.SPAN;
    }

    public void shutdownEvent() {
        this.type = SHUTDOWN;
    }

    @Override
    public String toString() {
        StringBuilder description = new StringBuilder();
        description.append("Type: ").append(type);
        if (transaction != null) {
            description.append(", ").append(transaction.toString());
        } else if (span != null) {
            description.append(", ").append(span.toString());
        }
        return description.toString();
    }

    @Nullable
    public JsonWriter getJsonWriter() {
        return jsonWriter;
    }

    public void setJsonWriter(@Nullable JsonWriter jsonWriter) {
        this.jsonWriter = jsonWriter;
        this.type = JSON_WRITER;
    }

    public void end() {
        if (transaction != null) {
            transaction.decrementReferences();
        } else if (span != null) {
            span.decrementReferences();
        } else if (error != null) {
            error.recycle();
        }
    }

    enum ReportingEventType {
        FLUSH, TRANSACTION, SPAN, ERROR, SHUTDOWN, JSON_WRITER
    }
}
