package com.iwish.server.db;

import com.iwish.client.model.WishItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WishItemDao {

    public List<WishItem> getByOwner(String ownerUsername) {
        List<WishItem> result = new ArrayList<>();
        String sql = "SELECT id, name, price, contributed_amount, owner_username " +
                "FROM wish_items WHERE owner_username = ? ORDER BY id";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, ownerUsername);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<WishItem> getById(int id) {
        String sql = "SELECT id, name, price, contributed_amount, owner_username FROM wish_items WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void add(String ownerUsername, String name, double price) {
        String sql = "INSERT INTO wish_items(name, price, contributed_amount, owner_username) VALUES (?, ?, 0, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setString(3, ownerUsername);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Only updates the row if it belongs to ownerUsername - owners can't edit each other's items. */
    public boolean update(String ownerUsername, int itemId, String newName, double newPrice) {
        String sql = "UPDATE wish_items SET name = ?, price = ? WHERE id = ? AND owner_username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setDouble(2, newPrice);
            ps.setInt(3, itemId);
            ps.setString(4, ownerUsername);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(String ownerUsername, int itemId) {
        String sql = "DELETE FROM wish_items WHERE id = ? AND owner_username = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, itemId);
            ps.setString(2, ownerUsername);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /** Adds a contribution and returns the item's contributed_amount/price AFTER the update. */
    public boolean addContribution(int itemId, double amount) {
        String sql = "UPDATE wish_items SET contributed_amount = contributed_amount + ? WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setDouble(1, amount);
            ps.setInt(2, itemId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private WishItem map(ResultSet rs) throws SQLException {
        return new WishItem(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getDouble("price"),
                rs.getDouble("contributed_amount"),
                rs.getString("owner_username")
        );
    }

    private Connection conn() {
        return Database.getConnection();
    }
}
