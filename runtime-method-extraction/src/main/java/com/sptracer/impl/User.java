package com.sptracer.impl;

import com.sptracer.Recyclable;

import javax.annotation.Nullable;


/**
 * User
 * <p>
 * Describes the authenticated User for a request.
 */
public class User implements Recyclable {

    /**
     * Domain of the logged in user
     */
    @Nullable
    private String domain;

    /**
     * Identifier of the logged in user, e.g. the primary key of the user
     */
    @Nullable
    private String id;
    /**
     * Email of the logged in user
     */
    @Nullable
    private String email;
    /**
     * The username of the logged in user
     */
    @Nullable
    private String username;


    /**
     * Domain of the logged in user
     */
    @Nullable
    public String getDomain() {
        return domain;
    }

    /**
     * Domain of the logged in user
     */
    public User withDomain(@Nullable String domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Identifier of the logged in user, e.g. the primary key of the user
     */
    @Nullable
    public String getId() {
        return id;
    }

    /**
     * Identifier of the logged in user, e.g. the primary key of the user
     */
    public User withId(@Nullable String id) {
        this.id = id;
        return this;
    }

    /**
     * Email of the logged in user
     */
    @Nullable
    public String getEmail() {
        return email;
    }

    /**
     * Email of the logged in user
     */
    public User withEmail(@Nullable String email) {
        this.email = email;
        return this;
    }

    /**
     * The username of the logged in user
     */
    @Nullable
    public String getUsername() {
        return username;
    }

    /**
     * The username of the logged in user
     */
    public User withUsername(@Nullable String username) {
        this.username = username;
        return this;
    }

    @Override
    public void resetState() {
        domain = null;
        id = null;
        email = null;
        username = null;
    }

    public void copyFrom(User other) {
        this.domain = other.domain;
        this.email = other.email;
        this.id = other.id;
        this.username = other.username;
    }

    public boolean hasContent() {
        return domain != null || id != null || email != null || username != null;
    }
}
