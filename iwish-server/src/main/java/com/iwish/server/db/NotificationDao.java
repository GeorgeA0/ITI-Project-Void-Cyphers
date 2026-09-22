package com.iwish.server.db;

import com.iwish.client.model.Notification;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {

    public void add(String username, String message) {
        String sql = "INSERT INTO notifications(username, message, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, message);
            ps.setString(3, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Most recent first, matching MockClientService's behaviour. */
    public List<Notification> getForUser(String username) {
        List<Notification> result = new ArrayList<>();
        String sql = "SELECT message, created_at FROM notifications WHERE username = ? ORDER BY id DESC";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    LocalDateTime time = LocalDateTime.parse(rs.getString("created_at"), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    result.add(new Notification(rs.getString("message"), time));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection conn() {
        return Database.getConnection();
    }
}
