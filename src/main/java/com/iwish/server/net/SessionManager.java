package com.iwish.server.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of which username is logged in on which handler, so a given
 * account can't be logged in twice at once and so the server always knows
 * who is currently online (useful if/when real-time push notifications are
 * added on top of the polling model used by GET_NOTIFICATIONS).
 */
public class SessionManager {

    private final Map<String, ClientHandler> online = new ConcurrentHashMap<>();

    public boolean isOnline(String username) {
        return online.containsKey(username);
    }

    public void register(String username, ClientHandler handler) {
        online.put(username, handler);
    }

    public void unregister(String username) {
        if (username != null) {
            online.remove(username);
        }
    }

    public int onlineCount() {
        return online.size();
    }
}
