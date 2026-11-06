package com.cinepass.ui;

import com.cinepass.dao.ActivityLogDao;
import com.cinepass.dao.UserDao;
import com.cinepass.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final ActivityLogDao logDao;
    private final Scanner scanner;
    private final User currentAdmin;

    public AdminMenu(UserDao userDao, ActivityLogDao logDao, Scanner scanner, User currentAdmin) {
        this.userDao = userDao;
        this.logDao = logDao;
        this.scanner = scanner;
        this.currentAdmin = currentAdmin;
    }

    public void start() {
        boolean running = true;
        while (running) {
            System.out.println();
            System.out.println("=== Admin Menu ===");
            System.out.println("1. View All Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User");
            System.out.println("4. Update User");
            System.out.println("5. Logout");
            int choice = readIntInput("Choose: ");
            switch (choice) {
                case 1 -> printUsers(userDao.getAllUsers());
                case 2 -> printUsers(userDao.getPendingUsers());
                case 3 -> approveUser();
                case 4 -> updateUser();
                case 5 -> {
                    System.out.println("Logged out.");
                    running = false;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void printUsers(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        String line = "+-------+-----------------+--------------+------------+";
        System.out.println(line);
        System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n", "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println(line);
        for (User u : users) {
            System.out.printf("| %-5s | %-15s | %-12s | %-10s |%n", u.id(), u.username(), u.roles(), u.status());
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

    private void approveUser() {
        List<User> pending = userDao.getPendingUsers();
        printUsers(pending);
        if (pending.isEmpty()) {
            return;
        }
        while (true) {
            int id = readIntInput("User ID: ");
            if (id == 0) {
                System.out.println("Cancelled.");
                return;
            }
            boolean found = pending.stream().anyMatch(u -> u.id() == id);
            if (!found) {
                System.out.println("User ID not found in pending list.");
                continue;
            }
            if (userDao.approveUser(id)) {
                System.out.println("User approved successfully.");
                logDao.logAction(currentAdmin.id(), "USER_APPROVED", "Admin " + currentAdmin.id() + " approved account for User ID " + id);
            } else {
                System.out.println("Approval failed.");
            }
            return;
        }
    }

    private void updateUser() {
        List<User> users = userDao.getAllUsers();
        printUsers(users);
        while (true) {
            int id = readIntInput("User ID: ");
            if (id == 0) {
                System.out.println("Cancelled.");
                return;
            }
            if (id == currentAdmin.id()) {
                System.out.println("WARNING: Modifying the currently active admin account is restricted.");
                continue;
            }
            boolean found = users.stream().anyMatch(u -> u.id() == id);
            if (!found) {
                System.out.println("User ID not found.");
                continue;
            }
            String role = readValidRole();
            if (role == null) {
                System.out.println("Cancelled.");
                return;
            }
            String status = readValidStatus();
            if (status == null) {
                System.out.println("Cancelled.");
                return;
            }
            if (userDao.updateUserRoleAndStatus(id, role, status)) {
                System.out.println("User updated successfully.");
                logDao.logAction(currentAdmin.id(), "USER_UPDATED", "Admin " + currentAdmin.id() + " changed User " + id + " to " + role + ", " + status);
            } else {
                System.out.println("Update failed.");
            }
            return;
        }
    }

    private String readValidRole() {
        while (true) {
            System.out.print("Role: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (input.equals("ADMIN") || input.equals("CASHIER") || input.equals("CUSTOMER")) {
                return input;
            }
            System.out.println("Invalid role.");
        }
    }

    private String readValidStatus() {
        while (true) {
            System.out.print("Status: ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("0")) {
                return null;
            }
            if (input.equals("APPROVED") || input.equals("PENDING")) {
                return input;
            }
            System.out.println("Invalid status.");
        }
    }
}
