package ua.com.kisit.course_project.Demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Service.AuthenticationService;

import java.util.Optional;
import java.util.Scanner;

/**
 * Demo — запускається тільки з профілем "demo":
 *   java -jar app.jar --spring.profiles.active=demo
 *
 * В звичайному режимі цей клас ІГНОРУЄТЬСЯ Spring Boot.
 */
@Component
@Profile("demo")   // FIXED: не заважає нормальному запуску додатку
public class AuthenticationDemo implements CommandLineRunner {

    // FIXED: Spring сам інжектує сервіс — більше не треба Connection вручну
    private final AuthenticationService authService;
    private final Scanner scanner = new Scanner(System.in);

    private String currentSessionToken = null;

    public AuthenticationDemo(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        System.out.println("=== Car Rental System - Authentication Demo ===\n");
        boolean running = true;

        while (running) {
            if (currentSessionToken == null) {
                System.out.println("\n=== Menu (Not logged in) ===");
                System.out.println("1. Login");
                System.out.println("2. Register");
                System.out.println("0. Exit");
                System.out.print("Choose option: ");

                switch (getIntInput()) {
                    case 1 -> handleLogin();
                    case 2 -> handleRegistration();
                    case 0 -> { running = false; System.out.println("Goodbye!"); }
                    default -> System.out.println("Invalid option!");
                }
            } else {
                Optional<User> userOpt = authService.validateSession(currentSessionToken);
                if (userOpt.isEmpty()) {
                    System.out.println("Session expired. Please login again.");
                    currentSessionToken = null;
                    continue;
                }

                User user = userOpt.get();
                System.out.println("\n=== Menu (Logged in as: " + user.getEmail() + ") ===");
                System.out.println("Role: " + user.getRole().getDisplayName());
                System.out.println("1. View Profile");
                System.out.println("2. Change Password");
                System.out.println("3. Logout");
                System.out.println("0. Exit");
                System.out.print("Choose option: ");

                switch (getIntInput()) {
                    case 1 -> handleViewProfile(user);
                    case 2 -> handleChangePassword(user);
                    case 3 -> handleLogout();
                    case 0 -> { running = false; System.out.println("Goodbye!"); }
                    default -> System.out.println("Invalid option!");
                }
            }
        }
        scanner.close();
    }

    private void handleLogin() {
        System.out.println("\n=== Login ===");
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            currentSessionToken = authService.login(email, password);
            System.out.println("✓ Login successful!");
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private void handleRegistration() {
        System.out.println("\n=== Registration ===");
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

        try {
            authService.register(email, password, UserRole.CLIENT);
            System.out.println("✓ Registration successful! You can now login.");
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private void handleViewProfile(User user) {
        System.out.println("\n=== Profile ===");
        System.out.println("User ID: " + user.getUserId());
        System.out.println("Email:   " + user.getEmail());
        System.out.println("Role:    " + user.getRole().getDisplayName());
        System.out.println("Active:  " + (user.isActive() ? "Yes" : "No"));
        System.out.println("Created: " + user.getCreatedAt());
    }

    private void handleChangePassword(User user) {
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

        try {
            boolean changed = authService.changePassword(user.getUserId(), oldPassword, newPassword);
            if (changed) {
                System.out.println("✓ Password changed! Please login again.");
                currentSessionToken = null;
            } else {
                System.out.println("✗ Failed to change password.");
            }
        } catch (Exception e) {
            System.out.println("✗ " + e.getMessage());
        }
    }

    private void handleLogout() {
        if (currentSessionToken != null) {
            authService.logout(currentSessionToken);
            currentSessionToken = null;
            System.out.println("✓ Logged out successfully!");
        }
    }

    private int getIntInput() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}