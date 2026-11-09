package com.cinepass.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public static void initialize() {
        String usersTable = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password TEXT NOT NULL, "
                + "roles TEXT NOT NULL, "
                + "status TEXT NOT NULL DEFAULT 'PENDING')";
        String logsTable = "CREATE TABLE IF NOT EXISTS activity_logs ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "action_type TEXT NOT NULL, "
                + "description TEXT NOT NULL, "
                + "timestamp TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), "
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE)";
        String bookingsTable = "CREATE TABLE IF NOT EXISTS bookings ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "processed_by INTEGER NOT NULL, "
                + "movie_title TEXT NOT NULL, "
                + "seat_numbers TEXT NOT NULL, "
                + "ticket_qty INTEGER NOT NULL, "
                + "total_amount REAL NOT NULL, "
                + "payment_status TEXT NOT NULL DEFAULT 'PENDING', "
                + "booking_date TEXT NOT NULL DEFAULT (datetime('now', 'localtime')), "
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, "
                + "FOREIGN KEY (processed_by) REFERENCES users(id))";
        String defaultAdmin = "INSERT OR IGNORE INTO users (username, password, roles, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(usersTable);
            st.execute(logsTable);
            st.execute(bookingsTable);
            try (PreparedStatement ps = conn.prepareStatement(defaultAdmin)) {
                ps.setString(1, "admin");
                ps.setString(2, "admin123");
                ps.setString(3, "ADMIN");
                ps.setString(4, "APPROVED");
                ps.executeUpdate();
            }
            System.out.println("Database initialized.");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
