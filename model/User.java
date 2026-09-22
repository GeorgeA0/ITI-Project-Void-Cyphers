package com.iwish.client.model;

import java.io.Serializable;

/**
 * Represents the logged-in user (or any user we know the username of).
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;

    public User(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return username;
    }
}
