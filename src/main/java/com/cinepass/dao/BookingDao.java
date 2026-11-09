package com.cinepass.dao;

import com.cinepass.model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {

    public int createBooking(Booking booking) {
        String sql = "INSERT INTO bookings (user_id, processed_by, movie_title, seat_numbers, ticket_qty, total_amount, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String lastId = "SELECT last_insert_rowid()";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, booking.userId());
            ps.setInt(2, booking.processedBy());
            ps.setString(3, booking.movieTitle());
            ps.setString(4, booking.seatNumbers());
            ps.setInt(5, booking.ticketQty());
            ps.setDouble(6, booking.totalAmount());
            ps.setString(7, booking.paymentStatus());
            ps.executeUpdate();
            try (PreparedStatement idPs = conn.prepareStatement(lastId);
                 ResultSet rs = idPs.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return -1;
    }

    public boolean updatePaymentStatus(int bookingId, String newStatus) {
        String sql = "UPDATE bookings SET payment_status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.toUpperCase());
            ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }

    public Booking getBookingById(int bookingId) {
        String sql = "SELECT id, user_id, processed_by, movie_title, seat_numbers, ticket_qty, total_amount, payment_status, booking_date FROM bookings WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapBooking(rs);
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return null;
    }

    public List<Booking> getAllBookings() {
        String sql = "SELECT id, user_id, processed_by, movie_title, seat_numbers, ticket_qty, total_amount, payment_status, booking_date FROM bookings ORDER BY id DESC";
        List<Booking> bookings = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bookings.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return bookings;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getInt("processed_by"),
                rs.getString("movie_title"),
                rs.getString("seat_numbers"),
                rs.getInt("ticket_qty"),
                rs.getDouble("total_amount"),
                rs.getString("payment_status"),
                rs.getString("booking_date"));
    }
}
