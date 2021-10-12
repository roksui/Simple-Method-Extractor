package com.sptracer.configuration.converter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ByteValue {

    public static final Pattern BYTE_PATTERN = Pattern.compile("^(\\d+)(b|kb|mb|gb)$");

    private final long bytes;
    private final String byteString;

    public static ByteValue of(String byteString) {
        byteString = byteString.toLowerCase();
        Matcher matcher = BYTE_PATTERN.matcher(byteString);
        if (matcher.matches()) {
            long value = Long.parseLong(matcher.group(1));
            return new ByteValue(byteString, value * getUnitMultiplier(matcher.group(2)));
        } else {
            throw new IllegalArgumentException("Invalid byte value '" + byteString + "'");
        }
    }

    private static int getUnitMultiplier(String unit) {
        switch (unit) {
            case "b":
                return 1;
            case "kb":
                return 1024;
            case "mb":
                return 1024 * 1024;
            case "gb":
                return 1024 * 1024 * 1024;
            default:
                throw new IllegalStateException("Byte unit '" + unit + "' is unknown");
        }
    }

    private ByteValue(String byteString, long bytes) {
        this.byteString = byteString;
        this.bytes = bytes;
    }

    public long getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return byteString;
    }
}
