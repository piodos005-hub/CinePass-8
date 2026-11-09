package com.cinepass.model;

public record Booking(int id, int userId, int processedBy, String movieTitle, String seatNumbers, int ticketQty, double totalAmount, String paymentStatus, String bookingDate) {
}
