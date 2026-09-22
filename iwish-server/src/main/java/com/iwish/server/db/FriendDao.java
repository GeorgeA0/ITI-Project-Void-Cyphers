package com.iwish.server.db;

import com.iwish.client.model.FriendRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FriendDao {

    public List<String> getFriends(String username) {
        List<String> result = new ArrayList<>();
        String sql = "SELECT friend_username FROM friends WHERE username = ? ORDER BY friend_username";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString(1));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean areFriends(String a, String b) {
        String sql = "SELECT 1 FROM friends WHERE username = ? AND friend_username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, a);
            ps.setString(2, b);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<FriendRequest> getPendingRequests(String toUsername) {
        List<FriendRequest> result = new ArrayList<>();
        String sql = "SELECT from_username FROM friend_requests WHERE to_username = ? ORDER BY id";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, toUsername);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new FriendRequest(rs.getString(1)));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean sendRequest(String fromUsername, String toUsername, UserDao userDao) {
        if (toUsername == null || toUsername.equals(fromUsername) || !userDao.exists(toUsername)) {
            return false;
        }
        if (areFriends(fromUsername, toUsername) || hasPendingRequest(fromUsername, toUsername)) {
            return false;
        }
        String sql = "INSERT INTO friend_requests(from_username, to_username) VALUES (?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, fromUsername);
            ps.setString(2, toUsername);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // duplicate request, blocked by the UNIQUE constraint
        }
    }

    private boolean hasPendingRequest(String fromUsername, String toUsername) {
        String sql = "SELECT 1 FROM friend_requests WHERE from_username = ? AND to_username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, fromUsername);
            ps.setString(2, toUsername);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void acceptRequest(String toUsername, String fromUsername) {
        deleteRequest(fromUsername, toUsername);
        addFriendPair(toUsername, fromUsername);
    }

    public void declineRequest(String toUsername, String fromUsername) {
        deleteRequest(fromUsername, toUsername);
    }

    private void deleteRequest(String fromUsername, String toUsername) {
        String sql = "DELETE FROM friend_requests WHERE from_username = ? AND to_username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, fromUsername);
            ps.setString(2, toUsername);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addFriendPair(String a, String b) {
        String sql = "INSERT OR IGNORE INTO friends(username, friend_username) VALUES (?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, a);
            ps.setString(2, b);
            ps.executeUpdate();
            ps.setString(1, b);
            ps.setString(2, a);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeFriend(String username, String friendUsername) {
        String sql = "DELETE FROM friends WHERE (username = ? AND friend_username = ?) OR (username = ? AND friend_username = ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, friendUsername);
            ps.setString(3, friendUsername);
            ps.setString(4, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection conn() {
        return Database.getConnection();
    }
}
