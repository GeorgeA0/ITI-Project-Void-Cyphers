package com.iwish.client.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.iwish.client.model.FriendRequest;
import com.iwish.client.model.Notification;
import com.iwish.client.model.WishItem;

/**
 * In-memory implementation of ClientService.
 *
 * This lets the whole GUI be demoed/tested without a running server. It seeds a couple of
 * fake accounts and wish lists so the app doesn't look empty on first run.
 */
public class MockClientService implements ClientService {

    private final Map<String, String> accounts = new HashMap<>();          // username -> password
    private final Map<String, Set<String>> friends = new HashMap<>();      // username -> friends
    private final Map<String, List<FriendRequest>> pendingRequests = new HashMap<>();
    private final Map<String, List<WishItem>> wishLists = new HashMap<>(); // username -> items
    private final List<Notification> notifications = new ArrayList<>();

    private String currentUsername;
    private int nextItemId = 1;

    public MockClientService() {
        seedDemoData();
    }

    private void seedDemoData() {
        accounts.put("george", "1234");
        accounts.put("mona", "1234");
        accounts.put("sam", "1234");

        friends.put("george", new HashSet<>(Set.of("mona")));
        friends.put("mona", new HashSet<>(Set.of("george")));
        friends.put("sam", new HashSet<>());

        pendingRequests.put("george", new ArrayList<>(List.of(new FriendRequest("sam"))));
        pendingRequests.put("mona", new ArrayList<>());
        pendingRequests.put("sam", new ArrayList<>());

        wishLists.put("george", new ArrayList<>(List.of(
                new WishItem(nextItemId++, "Mechanical Keyboard", 60.0, 0, "george")
        )));
        wishLists.put("mona", new ArrayList<>(List.of(
                new WishItem(nextItemId++, "Wireless Headphones", 90.0, 20.0, "mona"),
                new WishItem(nextItemId++, "Sketchbook Set", 25.0, 0, "mona")
        )));
        wishLists.put("sam", new ArrayList<>(List.of(
                new WishItem(nextItemId++, "Chess Board", 30.0, 0, "sam")
        )));
    }

    // ---------------- Auth ----------------

    @Override
    public boolean register(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }
        if (accounts.containsKey(username)) {
            return false; // username taken
        }
        accounts.put(username, password);
        friends.put(username, new HashSet<>());
        pendingRequests.put(username, new ArrayList<>());
        wishLists.put(username, new ArrayList<>());
        return true;
    }

    @Override
    public boolean login(String username, String password) {
        if (accounts.containsKey(username) && accounts.get(username).equals(password)) {
            currentUsername = username;
            return true;
        }
        return false;
    }

    @Override
    public void logout() {
        currentUsername = null;
    }

    @Override
    public String getCurrentUsername() {
        return currentUsername;
    }

    // ---------------- Friends ----------------

    @Override
    public List<String> getFriends() {
        return new ArrayList<>(friends.getOrDefault(currentUsername, Set.of()));
    }

    @Override
    public List<FriendRequest> getPendingRequests() {
        return pendingRequests.getOrDefault(currentUsername, new ArrayList<>());
    }

    @Override
    public boolean sendFriendRequest(String username) {
        if (username == null || username.equals(currentUsername) || !accounts.containsKey(username)) {
            return false;
        }
        List<FriendRequest> theirRequests = pendingRequests.computeIfAbsent(username, k -> new ArrayList<>());
        boolean alreadySent = theirRequests.stream().anyMatch(r -> r.getFromUsername().equals(currentUsername));
        boolean alreadyFriends = friends.getOrDefault(currentUsername, Set.of()).contains(username);
        if (alreadySent || alreadyFriends) {
            return false;
        }
        theirRequests.add(new FriendRequest(currentUsername));
        return true;
    }

    @Override
    public void acceptFriendRequest(String username) {
        List<FriendRequest> mine = pendingRequests.get(currentUsername);
        if (mine != null) {
            mine.removeIf(r -> r.getFromUsername().equals(username));
        }
        friends.computeIfAbsent(currentUsername, k -> new HashSet<>()).add(username);
        friends.computeIfAbsent(username, k -> new HashSet<>()).add(currentUsername);
    }

    @Override
    public void declineFriendRequest(String username) {
        List<FriendRequest> mine = pendingRequests.get(currentUsername);
        if (mine != null) {
            mine.removeIf(r -> r.getFromUsername().equals(username));
        }
    }

    @Override
    public void removeFriend(String username) {
        friends.getOrDefault(currentUsername, Set.of()).remove(username);
        friends.getOrDefault(username, Set.of()).remove(currentUsername);
    }

    // ---------------- My wish list ----------------

    @Override
    public List<WishItem> getMyWishList() {
        return wishLists.getOrDefault(currentUsername, new ArrayList<>());
    }

    @Override
    public void addWishItem(String name, double price) {
        wishLists.computeIfAbsent(currentUsername, k -> new ArrayList<>())
                .add(new WishItem(nextItemId++, name, price, 0, currentUsername));
    }

    @Override
    public void updateWishItem(int itemId, String newName, double newPrice) {
        for (WishItem item : wishLists.getOrDefault(currentUsername, List.of())) {
            if (item.getId() == itemId) {
                item.setName(newName);
                item.setPrice(newPrice);
                return;
            }
        }
    }

    @Override
    public void deleteWishItem(int itemId) {
        List<WishItem> list = wishLists.get(currentUsername);
        if (list != null) {
            list.removeIf(item -> item.getId() == itemId);
        }
    }

    // ---------------- Friends' wish lists & contributions ----------------زز

    @Override
    public List<WishItem> getFriendWishList(String friendUsername) {
        return wishLists.getOrDefault(friendUsername, new ArrayList<>());
    }

    @Override
    public boolean contribute(int itemId, double amount) {
        if (amount <= 0) {
            return false;
        }
        for (Map.Entry<String, List<WishItem>> entry : wishLists.entrySet()) {
            for (WishItem item : entry.getValue()) {
                if (item.getId() == itemId) {
                    boolean wasFunded = item.isFullyFunded();
                    item.addContribution(amount);
                    notifications.add(new Notification(
                            "[As Buyer] Your $" + amount + " contribution to \"" + item.getName() + "\" was recorded."));
                    if (!wasFunded && item.isFullyFunded()) {
                        notifications.add(new Notification(
                                "[As Buyer] \"" + item.getName() + "\" is now fully funded!"));
                        // In a real client/server app this would also notify the item owner
                        // (the receiver) through their own connection/session.
                    }
                    return true;
                }
            }
        }
        return false;
    }

    // ---------------- Notifications ----------------

    @Override
    public List<Notification> getNotifications() {
        List<Notification> mine = new ArrayList<>(notifications);
        Collections.reverse(mine);
        return mine;
    }
}
