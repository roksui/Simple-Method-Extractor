package com.sptracer;

import javax.annotation.Nullable;

public interface HeaderGetter<T, C> {

    @Nullable
    T getFirstHeader(String headerName, C carrier);

    /**
     * Calls the consumer for each header value with the given key
     * until all entries have been processed or the action throws an exception.
     * <p>
     * The third parameter lets callers pass in a stateful object to be modified with header values,
     * so the {@link HeaderConsumer} implementation itself can be stateless and potentially reusable.
     * </p>
     *
     * @param headerName the name of the header
     * @param carrier    the object containing the headers
     * @param state      the object to be passed as the second parameter to each invocation on the specified consumer
     * @param consumer   the action to be performed for each header value
     * @param <S>        the type of the state object
     */
    <S> void forEach(String headerName, C carrier, S state, HeaderConsumer<T, S> consumer);

    interface HeaderConsumer<T, S> {
        void accept(@Nullable T headerValue, S state);
    }
}
