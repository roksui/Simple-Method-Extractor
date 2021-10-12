package com.sptracer.impl;

import javax.annotation.Nullable;

public interface BinaryHeaderSetter<C> extends HeaderSetter<byte[], C> {

    /**
     * Since the implementation itself knows the intrinsics of the headers and carrier lifecycle and handling, it should
     * be responsible for providing a byte array. This enables the implementation to cache byte arrays wherever required
     * and possible.
     * <p>
     * NOTE: if this method returns null, the tracer will allocate a buffer for each header.
     *
     * @param headerName the header name for which the byte array is required
     * @param length     the length of the required byte array
     * @return a byte array with the requested length, or null if header-value-buffer is not supported.
     */
    @Nullable
    byte[] getFixedLengthByteArray(String headerName, int length);
}