package com.sptracer.configuration.converter;

import com.sptracer.configuration.ConfigurationOption;

public class ByteValueConverter extends AbstractValueConverter<ByteValue> {

    public static final ByteValueConverter INSTANCE = new ByteValueConverter();

    public static ConfigurationOption.ConfigurationOptionBuilder<ByteValue> byteOption() {
        return ConfigurationOption.builder(INSTANCE, ByteValue.class);
    }

    private ByteValueConverter() {
    }

    @Override
    public ByteValue convert(String s) throws IllegalArgumentException {
        return ByteValue.of(s);
    }

    @Override
    public String toString(ByteValue value) {
        return value.toString();
    }
}
