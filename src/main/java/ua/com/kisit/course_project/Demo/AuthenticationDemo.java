package ua.com.kisit.course_project.Demo;

import ua.com.kisit.course_project.Controller.AuthenticationController;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;
import ua.com.kisit.course_project.Repository.UserRepositoryImpl;
import ua.com.kisit.course_project.Repository.UserSessionRepository;
import ua.com.kisit.course_project.Repository.UserSessionRepositoryImpl;
import ua.com.kisit.course_project.Service.AuthenticationService;
import ua.com.kisit.course_project.Utility.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * Demo application showing authentication system usage
 * This is an example of how to use the authentication system
 */
public class AuthenticationDemo {

    private static AuthenticationController authController;
    private static Scanner scanner;

    public static void main(String[] args) {
        scanner = new Scanner(System.in);

        try {
            // Initialize database connection
            Connection connection = DatabaseConnection.getConnection();
            System.out.println("=== Car Rental System - Authentication Demo ===\n");

            // Initialize repositories
            UserRepository userRepository = new UserRepositoryImpl(connection);
            UserSessionRepository sessionRepository = new UserSessionRepositoryImpl(connection);

            // Initialize services
            AuthenticationService authService = new AuthenticationService(
                    userRepository,
                    sessionRepository
            );

            // Initialize controller
            authController = new AuthenticationController(authService);

            // Run demo menu
            runMenu();

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
            DatabaseConnection.closeConnection();
        }
    }

    private static void runMenu() {
        boolean running = true;

        while (running) {
            if (!authController.isLoggedIn()) {
                System.out.println("\n=== Menu (Not logged in) ===");
                System.out.println("1. Login");
                System.out.println("2. Register as Client");
                System.out.println("3. Register as Admin");
                System.out.println("0. Exit");
                System.out.print("Choose option: ");

                int choice = getIntInput();

                switch (choice) {
                    case 1:
                        handleLogin();
                        break;
                    case 2:
                        handleRegistration(UserRole.CLIENT);
                        break;
                    case 3:
                        handleRegistration(UserRole.ADMIN);
                        break;
                    case 0:
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option!");
                }
            } else {
                User currentUser = authController.getCurrentUser();
                System.out.println("\n=== Menu (Logged in as: " + currentUser.getEmail() + ") ===");
                System.out.println("Role: " + currentUser.getRole().getDisplayName());
                System.out.println("1. View Profile");
                System.out.println("2. Change Password");
                System.out.println("3. Logout");
                System.out.println("4. Logout from all devices");

                if (authController.isCurrentUserAdmin()) {
                    System.out.println("5. Admin Panel (example)");
                } else {
                    System.out.println("5. Browse Cars (example)");
                }

                System.out.println("0. Exit");
                System.out.print("Choose option: ");

                int choice = getIntInput();

                switch (choice) {
                    case 1:
                        handleViewProfile();
                        break;
                    case 2:
                        handleChangePassword();
                        break;
                    case 3:
                        handleLogout();
                        break;
                    case 4:
                        handleLogoutAll();
                        break;
                    case 5:
                        handleRoleSpecificAction();
                        break;
                    case 0:
                        running = false;
                        System.out.println("Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option!");
                }
            }
        }
    }

    private static void handleLogin() {
        System.out.println("\n=== Login ===");
        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (authController.login(email, password)) {
            System.out.println("✓ Login successful!");
        } else {
            System.out.println("✗ Login failed!");
        }
    }

    private static void handleRegistration(UserRole role) {
        System.out.println("\n=== Registration (" + role.getDisplayName() + ") ===");
        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password (min 6 characters): ");
        String password = scanner.nextLine();

        System.out.print("Confirm password: ");
        String confirmPassword = scanner.nextLine();

        if (!password.equals(confirmPassword)) {
            System.out.println("✗ Passwords do not match!");
            return;
        }

        if (authController.registerUser(email, password, role)) {
            System.out.println("✓ Registration successful! You can now login.");
        } else {
            System.out.println("✗ Registration failed!");
        }
    }

    private static void handleViewProfile() {
        User user = authController.getCurrentUser();
        if (user != null) {
            System.out.println("\n=== Profile ===");
            System.out.println("User ID: " + user.getUserId());
            System.out.println("Email: " + user.getEmail());
            System.out.println("Role: " + user.getRole().getDisplayName());
            System.out.println("Active: " + (user.isActive() ? "Yes" : "No"));
            System.out.println("Created: " + user.getCreatedAt());
        }
    }

    private static void handleChangePassword() {
        System.out.println("\n=== Change Password ===");
        System.out.print("Old password: ");
        String oldPassword = scanner.nextLine();

        System.out.print("New password: ");
        String newPassword = scanner.nextLine();

        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("✗ Passwords do not match!");
            return;
        }

        if (authController.changePassword(oldPassword, newPassword)) {
            System.out.println("✓ Password changed successfully!");
        } else {
            System.out.println("✗ Password change failed!");
        }
    }

    private static void handleLogout() {
        if (authController.logout()) {
            System.out.println("✓ Logged out successfully!");
        } else {
            System.out.println("✗ Logout failed!");
        }
    }

    private static void handleLogoutAll() {
        System.out.print("Are you sure you want to logout from all devices? (y/n): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("y")) {
            if (authController.logoutAll()) {
                System.out.println("✓ Logged out from all devices!");
            } else {
                System.out.println("✗ Logout failed!");
            }
        }
    }

    private static void handleRoleSpecificAction() {
        try {
            if (authController.isCurrentUserAdmin()) {
                authController.requireAdmin();
                System.out.println("\n=== Admin Panel ===");
                System.out.println("This is where admin features would be implemented");
                System.out.println("- Manage cars");
                System.out.println("- View all orders");
                System.out.println("- Manage damage reports");
                System.out.println("- View statistics");
            } else {
                authController.requireClient();
                System.out.println("\n=== Browse Cars ===");
                System.out.println("This is where client features would be implemented");
                System.out.println("- View available cars");
                System.out.println("- Make rental order");
                System.out.println("- View my orders");
                System.out.println("- Make payment");
            }
        } catch (SecurityException e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private static int getIntInput() {
        try {
            String input = scanner.nextLine();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}