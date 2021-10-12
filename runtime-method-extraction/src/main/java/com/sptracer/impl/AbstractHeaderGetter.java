package com.sptracer.impl;

import com.sptracer.HeaderGetter;

public abstract class AbstractHeaderGetter<T, C> implements HeaderGetter<T, C> {
    @Override
    public <S> void forEach(String headerName, C carrier, S state, HeaderGetter.HeaderConsumer<T, S> consumer) {
        T firstHeader = getFirstHeader(headerName, carrier);
        if (firstHeader != null) {
            consumer.accept(firstHeader, state);
        }
    }
}
