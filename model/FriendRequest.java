package com.iwish.client.model;

import java.io.Serializable;

public class FriendRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fromUsername;

    public FriendRequest(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    @Override
    public String toString() {
        return fromUsername;
    }
}
