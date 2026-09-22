package com.iwish.client.service;

import java.util.List;

import com.iwish.client.model.FriendRequest;
import com.iwish.client.model.Notification;
import com.iwish.client.model.WishItem;

/**
 * Everything the GUI needs from "the backend".
 *
 * This project only asks for the CLIENT, so {@link MockClientService} implements this
 * with in-memory data instead of real sockets/DB calls. When the server is ready, write a
 * SocketClientService that implements this same interface (talking to the server over
 * Sockets/ObjectStreams) and swap it in Main.java - none of the controllers need to change.
 */
public interface ClientService {

    // ---- Auth ----
    boolean register(String username, String password);

    boolean login(String username, String password);

    void logout();

    String getCurrentUsername();

    // ---- Friends ----
    List<String> getFriends();

    List<FriendRequest> getPendingRequests();

    boolean sendFriendRequest(String username);

    void acceptFriendRequest(String username);

    void declineFriendRequest(String username);

    void removeFriend(String username);

    // ---- My wish list ----
    List<WishItem> getMyWishList();

    void addWishItem(String name, double price);

    void updateWishItem(int itemId, String newName, double newPrice);

    void deleteWishItem(int itemId);

    // ---- Friends' wish lists & contributions ----
    List<WishItem> getFriendWishList(String friendUsername);

    boolean contribute(int itemId, double amount);

    // ---- Notifications ----ظظظ
    List<Notification> getNotifications();
}
