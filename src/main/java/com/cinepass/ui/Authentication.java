package com.cinepass.ui;

import com.cinepass.dao.ActivityLogDao;
import com.cinepass.dao.BookingDao;
import com.cinepass.dao.UserDao;
import com.cinepass.model.User;

import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final BookingDao bookingDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, BookingDao bookingDao, ActivityLogDao logDao, Scanner scanner) {
        this.userDao = userDao;
        this.bookingDao = bookingDao;
        this.logDao = logDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== CinePass ===");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> register();
                case "2" -> login();
                case "3" -> {
                    System.out.println("Goodbye.");
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            System.out.println("Username is required.");
            return;
        }
        if (userDao.usernameExists(username)) {
            System.out.println("Username already taken.");
            return;
        }
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            System.out.println("Password is required.");
            return;
        }
        System.out.println("1. ADMIN");
        System.out.println("2. CASHIER");
        System.out.println("3. CUSTOMER");
        System.out.print("Role: ");
        String role = switch (scanner.nextLine().trim()) {
            case "1" -> "ADMIN";
            case "2" -> "CASHIER";
            case "3" -> "CUSTOMER";
            default -> null;
        };
        if (role == null) {
            System.out.println("Invalid role.");
            return;
        }
        if (userDao.register(new User(0, username, password, role, "PENDING"))) {
            System.out.println("Registration successful. Status: PENDING");
            Integer newId = userDao.findIdByUsername(username);
            if (newId != null) {
                logDao.logAction(newId, "USER_REGISTERED", username + " registered as " + role + " with status PENDING");
            }
        } else {
            System.out.println("Registration failed.");
        }
    }

    private void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        User user = userDao.login(username, password);
        if (user == null) {
            System.out.println("Invalid username or password.");
            Integer knownId = userDao.findIdByUsername(username);
            if (knownId != null) {
                logDao.logAction(knownId, "LOGIN_FAILED", "Failed login attempt for " + username);
            }
            return;
        }
        if (!user.status().equals("APPROVED")) {
            System.out.println("Account is still " + user.status() + ".");
            logDao.logAction(user.id(), "LOGIN_BLOCKED", username + " tried to log in while " + user.status());
            return;
        }
        System.out.println("Welcome, " + user.username() + " (" + user.roles() + ")");
        logDao.logAction(user.id(), "LOGIN", "User " + user.username() + " (ID: " + user.id() + ") logged in.");
        route(user);
        logout(user);
    }

    private void logout(User user) {
        logDao.logAction(user.id(), "LOGOUT", "User " + user.username() + " (ID: " + user.id() + ") logged out.");
    }

    private void route(User user) {
        switch (user.roles()) {
            case "ADMIN" -> new AdminMenu(userDao, logDao, scanner, user).start();
            case "CASHIER" -> new CashierMenu(userDao, bookingDao, logDao, scanner, user).start();
            case "CUSTOMER" -> new CustomerMenu(userDao, scanner, user).start();
            default -> System.out.println("Unknown role.");
        }
    }
}
