package com.iwish.server.net;

import com.iwish.client.model.WishItem;
import com.iwish.protocol.Request;
import com.iwish.protocol.Response;
import com.iwish.server.db.FriendDao;
import com.iwish.server.db.NotificationDao;
import com.iwish.server.db.UserDao;
import com.iwish.server.db.WishItemDao;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.Optional;

/**
 * Handles one client's socket connection: one Request in, one Response out,
 * for as long as the connection stays open. Runs on its own thread so many
 * clients can be connected to the server at once (spec item 13: "Handles
 * the clients connections").
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final SessionManager sessionManager;
    private final UserDao userDao = new UserDao();
    private final FriendDao friendDao = new FriendDao();
    private final WishItemDao wishItemDao = new WishItemDao();
    private final NotificationDao notificationDao = new NotificationDao();

    private String currentUsername;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket, SessionManager sessionManager) {
        this.socket = socket;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        String remote = socket.getRemoteSocketAddress().toString();
        System.out.println("[Server] Client connected: " + remote);
        try (Socket s = socket;
             ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream())) {
            out = oos;
            out.flush();
            ObjectInputStream in = new ObjectInputStream(s.getInputStream());

            while (true) {
                Request request = (Request) in.readObject();
                Response response = dispatch(request);
                out.writeObject(response);
                out.reset(); // avoid re-sending stale cached object graphs
                out.flush();
            }
        } catch (EOFException | SocketException e) {
            // client closed the connection - normal shutdown path
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("[Server] Connection error with " + remote + ": " + e.getMessage());
        } finally {
            sessionManager.unregister(currentUsername);
            System.out.println("[Server] Client disconnected: " + remote);
        }
    }

    // ---- Handles the clients requests (spec item 14) ----
    private Response dispatch(Request req) {
        try {
            switch (req.getAction()) {
                case REGISTER: {
                    boolean ok = userDao.register(req.getString(0), req.getString(1));
                    return ok ? Response.ok(true) : Response.fail("That username is already taken.");
                }
                case LOGIN: {
                    String username = req.getString(0);
                    boolean ok = userDao.checkLogin(username, req.getString(1));
                    if (!ok) {
                        return Response.fail("Wrong username or password.");
                    }
                    currentUsername = username;
                    sessionManager.register(username, this);
                    return Response.ok(true);
                }
                case LOGOUT: {
                    sessionManager.unregister(currentUsername);
                    currentUsername = null;
                    return Response.ok();
                }

                case GET_FRIENDS:
                    return requireLogin(() -> Response.ok(friendDao.getFriends(currentUsername)));

                case GET_PENDING_REQUESTS:
                    return requireLogin(() -> Response.ok(friendDao.getPendingRequests(currentUsername)));

                case SEND_FRIEND_REQUEST:
                    return requireLogin(() -> {
                        boolean ok = friendDao.sendRequest(currentUsername, req.getString(0), userDao);
                        return ok ? Response.ok(true) : Response.fail("Could not send a request to that user.");
                    });

                case ACCEPT_FRIEND_REQUEST:
                    return requireLogin(() -> {
                        friendDao.acceptRequest(currentUsername, req.getString(0));
                        return Response.ok();
                    });

                case DECLINE_FRIEND_REQUEST:
                    return requireLogin(() -> {
                        friendDao.declineRequest(currentUsername, req.getString(0));
                        return Response.ok();
                    });

                case REMOVE_FRIEND:
                    return requireLogin(() -> {
                        friendDao.removeFriend(currentUsername, req.getString(0));
                        return Response.ok();
                    });

                case GET_MY_WISHLIST:
                    return requireLogin(() -> Response.ok(wishItemDao.getByOwner(currentUsername)));

                case ADD_WISH_ITEM:
                    return requireLogin(() -> {
                        wishItemDao.add(currentUsername, req.getString(0), req.getDouble(1));
                        return Response.ok();
                    });

                case UPDATE_WISH_ITEM:
                    return requireLogin(() -> {
                        wishItemDao.update(currentUsername, req.getInt(0), req.getString(1), req.getDouble(2));
                        return Response.ok();
                    });

                case DELETE_WISH_ITEM:
                    return requireLogin(() -> {
                        wishItemDao.delete(currentUsername, req.getInt(0));
                        return Response.ok();
                    });

                case GET_FRIEND_WISHLIST:
                    return requireLogin(() -> Response.ok(wishItemDao.getByOwner(req.getString(0))));

                case CONTRIBUTE:
                    return requireLogin(() -> handleContribute(req.getInt(0), req.getDouble(1)));

                case GET_NOTIFICATIONS:
                    return requireLogin(() -> Response.ok(notificationDao.getForUser(currentUsername)));

                default:
                    return Response.fail("Unknown action: " + req.getAction());
            }
        } catch (Exception e) {
            return Response.fail("Server error: " + e.getMessage());
        }
    }

    private Response handleContribute(int itemId, double amount) {
        if (amount <= 0) {
            return Response.fail("Enter a valid contribution amount.");
        }
        Optional<WishItem> before = wishItemDao.getById(itemId);
        if (before.isEmpty()) {
            return Response.fail("That wish list item no longer exists.");
        }
        boolean wasFunded = before.get().isFullyFunded();
        boolean ok = wishItemDao.addContribution(itemId, amount);
        if (!ok) {
            return Response.fail("Could not record that contribution.");
        }

        WishItem item = wishItemDao.getById(itemId).orElseThrow();
        notificationDao.add(currentUsername,
                "[As Buyer] Your $" + amount + " contribution to \"" + item.getName() + "\" was recorded.");

        if (!wasFunded && item.isFullyFunded()) {
            notificationDao.add(currentUsername,
                    "[As Buyer] \"" + item.getName() + "\" is now fully funded!");
            notificationDao.add(item.getOwnerUsername(),
                    "[As Receiver] Your item \"" + item.getName() + "\" has been fully funded by " + currentUsername + "!");
        }
        return Response.ok(true);
    }

    private Response requireLogin(java.util.function.Supplier<Response> action) {
        if (currentUsername == null) {
            return Response.fail("Not logged in.");
        }
        return action.get();
    }
}
