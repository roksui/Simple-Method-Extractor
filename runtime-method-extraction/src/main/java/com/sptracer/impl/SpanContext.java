package com.sptracer.impl;


public class SpanContext extends AbstractContext {

    /**
     * An object containing contextual data for database spans
     */
    private final Db db = new Db();

    /**
     * An object containing contextual data for outgoing HTTP spans
     */
    private final Http http = new Http();

    /**
     * An object containing contextual data for service maps
     */
    private final Destination destination = new Destination();

    /**
     * An object containing contextual data for database spans
     */
    public Db getDb() {
        return db;
    }

    /**
     * An object containing contextual data for outgoing HTTP spans
     */
    public Http getHttp() {
        return http;
    }

    /**
     * An object containing contextual data for service maps
     */
    public Destination getDestination() {
        return destination;
    }

    @Override
    public void resetState() {
        super.resetState();
        db.resetState();
        http.resetState();
        destination.resetState();
    }

    public boolean hasContent() {
        return super.hasContent() || db.hasContent() || http.hasContent() || destination.hasContent();
    }
}
