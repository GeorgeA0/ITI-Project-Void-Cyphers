package com.iwish.client.service;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.net.Socket;
import java.util.Collections;
import java.util.List;

import com.iwish.client.model.FriendRequest;
import com.iwish.client.model.Notification;
import com.iwish.client.model.WishItem;
import com.iwish.protocol.Action;
import com.iwish.protocol.Request;
import com.iwish.protocol.Response;

/**
 * Real {@link ClientService}: every method sends one {@link Request} to the
 * i-Wish server over a socket and blocks for the matching {@link Response}.
 * Controllers never see this class directly - they only depend on
 * ClientService, so swapping {@link MockClientService} for this one (see
 * AppContext) is the only change needed to go from "demo data" to "talks to
 * the real server".
 */
public class SocketClientService implements ClientService {

    private final Socket socket;
    private final ObjectOutputStream out;
    private final ObjectInputStream in;
    private String currentUsername;

    public SocketClientService(String host, int port) throws IOException {
        socket = new Socket(host, port);
        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());
    }

    private synchronized Response send(Action action, Object... args) {
        try {
            out.writeObject(new Request(action, args));
            out.reset();
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new UncheckedIOException("Lost connection to the i-Wish server", new IOException(e));
        }
    }

    // ---- Auth ----

    @Override
    public boolean register(String username, String password) {
        return send(Action.REGISTER, username, password).isSuccess();
    }

    @Override
    public boolean login(String username, String password) {
        boolean ok = send(Action.LOGIN, username, password).isSuccess();
        if (ok) {
            currentUsername = username;
        }
        return ok;
    }

    @Override
    public void logout() {
        send(Action.LOGOUT);
        currentUsername = null;
    }

    @Override
    public String getCurrentUsername() {
        return currentUsername;
    }

    // ---- Friends ----ززز

    @Override
    public List<String> getFriends() {
        return unwrap(send(Action.GET_FRIENDS));
    }

    @Override
    public List<FriendRequest> getPendingRequests() {
        return unwrap(send(Action.GET_PENDING_REQUESTS));
    }

    @Override
    public boolean sendFriendRequest(String username) {
        return send(Action.SEND_FRIEND_REQUEST, username).isSuccess();
    }

    @Override
    public void acceptFriendRequest(String username) {
        send(Action.ACCEPT_FRIEND_REQUEST, username);
    }

    @Override
    public void declineFriendRequest(String username) {
        send(Action.DECLINE_FRIEND_REQUEST, username);
    }

    @Override
    public void removeFriend(String username) {
        send(Action.REMOVE_FRIEND, username);
    }

    // ---- My wish list ----

    @Override
    public List<WishItem> getMyWishList() {
        return unwrap(send(Action.GET_MY_WISHLIST));
    }

    @Override
    public void addWishItem(String name, double price) {
        send(Action.ADD_WISH_ITEM, name, price);
    }

    @Override
    public void updateWishItem(int itemId, String newName, double newPrice) {
        send(Action.UPDATE_WISH_ITEM, itemId, newName, newPrice);
    }

    @Override
    public void deleteWishItem(int itemId) {
        send(Action.DELETE_WISH_ITEM, itemId);
    }

    // ---- Friends' wish lists & contributions ----

    @Override
    public List<WishItem> getFriendWishList(String friendUsername) {
        return unwrap(send(Action.GET_FRIEND_WISHLIST, friendUsername));
    }

    @Override
    public boolean contribute(int itemId, double amount) {
        return send(Action.CONTRIBUTE, itemId, amount).isSuccess();
    }

    // ---- Notifications ----

    @Override
    public List<Notification> getNotifications() {
        return unwrap(send(Action.GET_NOTIFICATIONS));
    }

    private <T> List<T> unwrap(Response response) {
        if (!response.isSuccess()) {
            return Collections.emptyList();
        }
        List<T> data = response.getData();
        return data != null ? data : Collections.emptyList();
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }
}
