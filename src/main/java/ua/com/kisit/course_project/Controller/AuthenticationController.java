package ua.com.kisit.course_project.Controller;

import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Service.AuthenticationService;

import java.util.Optional;

/**
 * Controller for handling authentication requests
 * This class manages user login, registration, and session validation
 */
public class AuthenticationController {

    private final AuthenticationService authService;
    private String currentSessionToken;
    private User currentUser;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Handle user registration
     * @param email user email
     * @param password user password
     * @param role user role (CLIENT or ADMIN)
     * @return true if registration successful
     */
    public boolean registerUser(String email, String password, UserRole role) {
        try {
            User user = authService.register(email, password, role);
            return user != null;
        } catch (IllegalArgumentException e) {
            System.err.println("Registration error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Handle client registration (default role: CLIENT)
     * @param email user email
     * @param password user password
     * @return true if registration successful
     */
    public boolean registerClient(String email, String password) {
        return registerUser(email, password, UserRole.CLIENT);
    }

    /**
     * Handle user login
     * @param email user email
     * @param password user password
     * @return true if login successful
     */
    public boolean login(String email, String password) {
        try {
            String sessionToken = authService.login(email, password);

            if (sessionToken != null) {
                this.currentSessionToken = sessionToken;

                // Load current user
                Optional<User> userOptional = authService.validateSession(sessionToken);
                if (userOptional.isPresent()) {
                    this.currentUser = userOptional.get();
                    System.out.println("Login successful for user: " + currentUser.getEmail());
                    return true;
                }
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Login error: " + e.getMessage());
        }

        return false;
    }

    /**
     * Handle user logout
     * @return true if logout successful
     */
    public boolean logout() {
        if (currentSessionToken != null) {
            boolean success = authService.logout(currentSessionToken);

            if (success) {
                this.currentSessionToken = null;
                this.currentUser = null;
                System.out.println("Logout successful");
            }

            return success;
        }

        return false;
    }

    /**
     * Handle logout from all devices
     * @return true if logout successful
     */
    public boolean logoutAll() {
        if (currentUser != null) {
            boolean success = authService.logoutAll(currentUser.getUserId());

            if (success) {
                this.currentSessionToken = null;
                this.currentUser = null;
                System.out.println("Logged out from all devices");
            }

            return success;
        }

        return false;
    }

    /**
     * Change current user's password
     * @param oldPassword old password
     * @param newPassword new password
     * @return true if password changed successfully
     */
    public boolean changePassword(String oldPassword, String newPassword) {
        if (currentUser == null) {
            System.err.println("No user logged in");
            return false;
        }

        try {
            boolean success = authService.changePassword(
                    currentUser.getUserId(),
                    oldPassword,
                    newPassword
            );

            if (success) {
                System.out.println("Password changed successfully. Please login again.");
                this.currentSessionToken = null;
                this.currentUser = null;
            }

            return success;
        } catch (IllegalArgumentException e) {
            System.err.println("Password change error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if user is currently logged in
     * @return true if user is logged in
     */
    public boolean isLoggedIn() {
        if (currentSessionToken == null) {
            return false;
        }

        // Validate session
        Optional<User> userOptional = authService.validateSession(currentSessionToken);

        if (userOptional.isEmpty()) {
            this.currentSessionToken = null;
            this.currentUser = null;
            return false;
        }

        return true;
    }

    /**
     * Get current logged in user
     * @return current user or null
     */
    public User getCurrentUser() {
        if (isLoggedIn()) {
            return currentUser;
        }
        return null;
    }

    /**
     * Get current session token
     * @return session token or null
     */
    public String getCurrentSessionToken() {
        return currentSessionToken;
    }

    /**
     * Check if current user is admin
     * @return true if current user is admin
     */
    public boolean isCurrentUserAdmin() {
        return currentUser != null && authService.isAdmin(currentUser);
    }

    /**
     * Check if current user is client
     * @return true if current user is client
     */
    public boolean isCurrentUserClient() {
        return currentUser != null && authService.isClient(currentUser);
    }

    /**
     * Require admin role for current user
     * @throws SecurityException if user is not admin
     */
    public void requireAdmin() throws SecurityException {
        if (!isCurrentUserAdmin()) {
            throw new SecurityException("Access denied. Admin role required.");
        }
    }

    /**
     * Require client role for current user
     * @throws SecurityException if user is not client
     */
    public void requireClient() throws SecurityException {
        if (!isCurrentUserClient()) {
            throw new SecurityException("Access denied. Client role required.");
        }
    }

    /**
     * Require authenticated user
     * @throws SecurityException if user is not logged in
     */
    public void requireAuthentication() throws SecurityException {
        if (!isLoggedIn()) {
            throw new SecurityException("Access denied. Please login.");
        }
    }
}