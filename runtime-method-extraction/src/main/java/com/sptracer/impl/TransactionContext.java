package com.sptracer.impl;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Context
 * <p>
 * Any arbitrary contextual information regarding the event, captured by the agent, optionally provided by the user
 */
public class TransactionContext extends AbstractContext {

    private final Map<String, Object> custom = new ConcurrentHashMap<>();
    private final Response response = new Response();
    /**
     * Request
     * <p>
     * If a log record was generated as a result of a http request, the http interface can be used to collect this information.
     */
    private final Request request = new Request();
    /**
     * User
     * <p>
     * Describes the authenticated User for a request.
     */
    private final User user = new User();

    public void copyFrom(TransactionContext other) {
        super.copyFrom(other);
        response.copyFrom(other.response);
        request.copyFrom(other.request);
        user.copyFrom(other.user);
    }

    public Object getCustom(String key) {
        return custom.get(key);
    }

    public Response getResponse() {
        return response;
    }

    public void addCustom(String key, String value) {
        custom.put(key, value);
    }

    public void addCustom(String key, Number value) {
        custom.put(key, value);
    }

    public void addCustom(String key, boolean value) {
        custom.put(key, value);
    }

    public boolean hasCustom() {
        return !custom.isEmpty();
    }

    public Iterator<? extends Map.Entry<String, ?>> getCustomIterator() {
        return custom.entrySet().iterator();
    }

    /**
     * Request
     * <p>
     * If a log record was generated as a result of a http request, the http interface can be used to collect this information.
     */
    public Request getRequest() {
        return request;
    }

    /**
     * User
     * <p>
     * Describes the authenticated User for a request.
     */
    public User getUser() {
        return user;
    }
    @Override
    public void resetState() {
        super.resetState();
        custom.clear();
        response.resetState();
        request.resetState();
        user.resetState();
    }

    public void onTransactionEnd() {
        request.onTransactionEnd();
    }

}
