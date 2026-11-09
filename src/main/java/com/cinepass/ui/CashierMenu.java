package com.cinepass.ui;

import com.cinepass.dao.ActivityLogDao;
import com.cinepass.dao.BookingDao;
import com.cinepass.dao.UserDao;
import com.cinepass.model.Booking;
import com.cinepass.model.User;

import java.util.List;
import java.util.Scanner;

public class CashierMenu {
    private final UserDao userDao;
    private final BookingDao bookingDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;
    private final User currentCashier;

    public CashierMenu(UserDao userDao, BookingDao bookingDao, ActivityLogDao logDao, Scanner scanner, User currentCashier) {
        this.userDao = userDao;
        this.bookingDao = bookingDao;
        this.logDao = logDao;
        this.scanner = scanner;
        this.currentCashier = currentCashier;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Cashier Menu ===");
            System.out.println("1. Create Booking");
            System.out.println("2. Update Booking Status");
            System.out.println("3. View Bookings");
            System.out.println("4. Logout");
            int choice = readIntInput("Choose: ");
            switch (choice) {
                case 1 -> createBooking();
                case 2 -> updateBookingStatus();
                case 3 -> printBookings(bookingDao.getAllBookings());
                case 4 -> {
                    System.out.println("Logged out.");
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void createBooking() {
        User customer = null;
        while (customer == null) {
            int customerId = readIntInput("Customer ID: ");
            if (customerId == 0) {
                System.out.println("Cancelled.");
                return;
            }
            User found = userDao.getUserById(customerId);
            if (found == null || !found.roles().equals("CUSTOMER") || !found.status().equals("APPROVED")) {
                System.out.println("Approved customer not found.");
            } else {
                customer = found;
            }
        }
        System.out.print("Movie Title: ");
        String movie = scanner.nextLine().trim();
        System.out.print("Seat Numbers: ");
        String seats = scanner.nextLine().trim();
        if (movie.isEmpty() || seats.isEmpty()) {
            System.out.println("Movie title and seat numbers are required.");
            return;
        }
        int qty = readPositiveInt("Quantity: ");
        double price = readPositiveDouble("Ticket Price: ");
        double total = qty * price;
        int bookingId = bookingDao.createBooking(new Booking(0, customer.id(), currentCashier.id(), movie, seats, qty, total, "PENDING", null));
        if (bookingId > 0) {
            System.out.println("Booking created. Booking ID: " + bookingId);
            logDao.logAction(currentCashier.id(), "BOOKING_CREATED",
                    "Cashier " + currentCashier.id() + " created Booking #" + bookingId + " for Customer " + customer.id() + " - Total: P" + String.format("%.2f", total));
        } else {
            System.out.println("Booking failed.");
        }
    }

    private void updateBookingStatus() {
        printBookings(bookingDao.getAllBookings());
        while (true) {
            int id = readIntInput("Booking ID: ");
            if (id == 0) {
                System.out.println("Cancelled.");
                return;
            }
            Booking booking = bookingDao.getBookingById(id);
            if (booking == null) {
                System.out.println("Booking not found.");
                continue;
            }
            String newStatus = readValidStatus();
            if (newStatus == null) {
                System.out.println("Cancelled.");
                return;
            }
            if (newStatus.equals(booking.paymentStatus())) {
                System.out.println("Status is already " + newStatus + ".");
                return;
            }
            if (bookingDao.updatePaymentStatus(id, newStatus)) {
                System.out.println("Booking status updated.");
                logDao.logAction(currentCashier.id(), "BOOKING_STATUS_UPDATED",
                        "Updated Booking #" + id + " status from " + booking.paymentStatus() + " to " + newStatus);
            } else {
                System.out.println("Update failed.");
            }
            return;
        }
    }

    private void printBookings(List<Booking> bookings) {
        if (bookings.isEmpty()) {
            System.out.println("No bookings found.");
            return;
        }
        String line = "+" + "-".repeat(7) + "+" + "-".repeat(10) + "+" + "-".repeat(22) + "+" + "-".repeat(14)
                + "+" + "-".repeat(6) + "+" + "-".repeat(12) + "+" + "-".repeat(12) + "+";
        String format = "| %-5s | %-8s | %-20s | %-12s | %-4s | %-10s | %-10s |%n";
        System.out.println(line);
        System.out.printf(format, "ID", "CUSTOMER", "MOVIE", "SEATS", "QTY", "TOTAL", "STATUS");
        System.out.println(line);
        for (Booking b : bookings) {
            System.out.printf(format, b.id(), b.userId(), b.movieTitle(), b.seatNumbers(), b.ticketQty(),
                    String.format("%.2f", b.totalAmount()), b.paymentStatus());
        }
        System.out.println(line);
    }

    private int readIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
            }
        }
    }

    private int readPositiveInt(String prompt) {
        while (true) {
            int value = readIntInput(prompt);
            if (value > 0) {
                return value;
            }
            System.out.println("Invalid input.");
        }
    }

    private double readPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double value = Double.parseDouble(scanner.nextLine().trim());
                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException e) {
            }
            System.out.println("Invalid input.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Status: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (input.equals("PENDING") || input.equals("PAID") || input.equals("CANCELLED")) {
                return input;
            }
            System.out.println("Invalid status.");
        }
    }
}
