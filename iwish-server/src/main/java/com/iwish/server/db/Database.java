package com.iwish.server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Owns the single JDBC connection to the embedded SQLite database file and
 * creates the schema on first run. SQLite is used so the whole database is
 * one portable file (iwish.db) that can be handed in as the "database
 * scheme/backup" deliverable - no separate DB server needs to be installed.
 */
public final class Database {

    private static final String DB_FILE = "iwish.db";
    private static Connection connection;

    private Database() {
    }

    public static synchronized Connection getConnection() {
        if (connection == null) {
            connect();
        }
        return connection;
    }

    private static void connect() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON");
            }
            createSchema();
            System.out.println("[Database] Connected to " + DB_FILE);
        } catch (SQLException e) {
            throw new RuntimeException("Could not connect to the database", e);
        }
    }

    private static void createSchema() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password TEXT NOT NULL
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS friends (
                    username       TEXT NOT NULL REFERENCES users(username) ON DELETE CASCADE,
                    friend_username TEXT NOT NULL REFERENCES users(username) ON DELETE CASCADE,
                    PRIMARY KEY (username, friend_username)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS friend_requests (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    from_username TEXT NOT NULL REFERENCES users(username) ON DELETE CASCADE,
                    to_username   TEXT NOT NULL REFERENCES users(username) ON DELETE CASCADE,
                    UNIQUE (from_username, to_username)
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS wish_items (
                    id                 INTEGER PRIMARY KEY AUTOINCREMENT,
                    name               TEXT NOT NULL,
                    price              REAL NOT NULL,
                    contributed_amount REAL NOT NULL DEFAULT 0,
                    owner_username     TEXT NOT NULL REFERENCES users(username) ON DELETE CASCADE
                )
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id         INTEGER PRIMARY KEY AUTOINCREMENT,
                    username   TEXT NOT NULL REFERENCES users(username) ON DELETE CASCADE,
                    message    TEXT NOT NULL,
                    created_at TEXT NOT NULL
                )
            """);
        }
    }

    /** Lets an admin seed catalog items directly, per spec item 12 (db insertion). */
    public static void main(String[] args) throws SQLException {
        getConnection();
        System.out.println("Schema ready in " + DB_FILE);
    }
}
