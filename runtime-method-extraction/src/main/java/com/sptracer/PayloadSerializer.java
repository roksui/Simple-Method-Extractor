package com.sptracer;

import com.dslplatform.json.JsonWriter;
import com.sptracer.error.ErrorCapture;
import com.sptracer.impl.Span;
import com.sptracer.impl.Transaction;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

public interface PayloadSerializer {

    /**
     * Sets the output stream which the {@code *NdJson} methods should write to.
     *
     * @param os the {@link OutputStream} to which all contents are to be serialized
     */
    void setOutputStream(OutputStream os);

    /**
     * Blocking until this {@link PayloadSerializer} is ready for use.
     *
     * @throws Exception if blocking was interrupted, or timed out or an error occurred in the underlying implementation
     */
    void blockUntilReady() throws Exception;

    /**
     * Appends the serialized metadata to ND-JSON as a {@code metadata} line.
     * <p>
     * NOTE: Must be called after {@link PayloadSerializer#blockUntilReady()} was called and returned, otherwise the
     * cached serialized metadata may not be ready yet.
     * </p>
     *
     * @throws UninitializedException may be thrown if {@link PayloadSerializer#blockUntilReady()} was not invoked
     */
    void appendMetaDataNdJsonToStream() throws UninitializedException;

    /**
     * Appends the serialized metadata to the underlying {@link OutputStream}.
     * <p>
     * NOTE: Must be called after {@link PayloadSerializer#blockUntilReady()} was called and returned, otherwise the
     * cached serialized metadata may not be ready yet.
     * </p>
     *
     * @throws UninitializedException may be thrown if {@link PayloadSerializer#blockUntilReady()} was not invoked
     */
    void appendMetadataToStream() throws UninitializedException;

    void serializeTransactionNdJson(Transaction transaction);

    void serializeSpanNdJson(Span span);

    void serializeErrorNdJson(ErrorCapture error);

    /**
     * Flushes the {@link OutputStream} which has been set via {@link #setOutputStream(OutputStream)}
     * and detaches that {@link OutputStream} from the serializer.
     */
    void fullFlush() throws IOException;

    /**
     * Flushes content that has been written so far to the {@link OutputStream} which has been set
     * via {@link #setOutputStream(OutputStream)}, without flushing the {@link OutputStream} itself.
     * Subsequent serializations will be made to the same {@link OutputStream}.
     */
    void flushToOutputStream();

    /**
     * Gets the number of bytes which are currently buffered
     *
     * @return the number of bytes which are currently buffered
     */
    int getBufferSize();

    void serializeFileMetaData(File file);

    JsonWriter getJsonWriter();

    void writeBytes(byte[] bytes, int len);

    class UninitializedException extends Exception {
        public UninitializedException(String message) {
            super(message);
        }
    }
}
