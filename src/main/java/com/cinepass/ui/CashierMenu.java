package com.cinepass.ui;

import com.cinepass.dao.UserDao;
import com.cinepass.model.User;

import java.util.Scanner;

public class CashierMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final User currentUser;

    public CashierMenu(UserDao userDao, Scanner scanner, User currentUser) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.currentUser = currentUser;
    }

    public void start() {
        while (true) {
            System.out.println();
            System.out.println("=== Cashier Menu ===");
            System.out.println("1. Logout");
            System.out.print("Choose: ");
            if (scanner.nextLine().trim().equals("1")) {
                System.out.println("Logged out.");
                return;
            }
            System.out.println("Invalid choice.");
        }
    }
}
