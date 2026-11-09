package com.cinepass;

import com.cinepass.dao.ActivityLogDao;
import com.cinepass.dao.BookingDao;
import com.cinepass.dao.DatabaseInitializer;
import com.cinepass.dao.UserDao;
import com.cinepass.ui.Authentication;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        UserDao userDao = new UserDao();
        ActivityLogDao logDao = new ActivityLogDao();
        BookingDao bookingDao = new BookingDao();
        Scanner scanner = new Scanner(System.in);
        new Authentication(userDao, bookingDao, logDao, scanner).start();
        scanner.close();
    }
}
