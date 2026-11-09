package com.cinepass.dao;

import com.cinepass.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    public void logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public List<ActivityLog> getAllLogs() {
        String sql = "SELECT id, user_id, action_type, description, timestamp FROM activity_logs ORDER BY timestamp DESC, id DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                logs.add(mapLog(rs));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        String sql = "SELECT id, user_id, action_type, description, timestamp FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, id DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapLog(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return logs;
    }

    public boolean clearLogs() {
        String sql = "DELETE FROM activity_logs";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    private ActivityLog mapLog(ResultSet rs) throws SQLException {
        return new ActivityLog(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("action_type"),
                rs.getString("description"),
                rs.getString("timestamp"));
    }
}
