package ua.com.kisit.course_project.Service;

import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Entity.UserSession;
import ua.com.kisit.course_project.Repository.UserRepository;
import ua.com.kisit.course_project.Repository.UserSessionRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

/**
 * Service for handling user authentication and authorization
 */
public class AuthenticationService {

    private final UserRepository userRepository;
    private final UserSessionRepository sessionRepository;
    private static final int SESSION_DURATION_HOURS = 24;

    public AuthenticationService(UserRepository userRepository, UserSessionRepository sessionRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
    }

    /**
     * Register new user
     * @param email user email
     * @param password plain text password
     * @param role user role
     * @return created user or null if registration failed
     */
    public User register(String email, String password, UserRole role) {
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Користувач з таким email вже існує");
        }

        // Validate email format
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Невірний формат email");
        }

        // Validate password strength
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Пароль повинен містити мінімум 6 символів");
        }

        // Hash password
        String passwordHash = hashPassword(password);

        // Create new user
        User user = new User(email, passwordHash, role);

        return userRepository.save(user);
    }

    /**
     * Login user and create session
     * @param email user email
     * @param password plain text password
     * @return session token or null if login failed
     */
    public String login(String email, String password) {
        // Find user by email
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Невірний email або пароль");
        }

        User user = userOptional.get();

        // Check if user is active
        if (!user.isActive()) {
            throw new IllegalStateException("Обліковий запис деактивовано");
        }

        // Verify password
        if (!verifyPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Невірний email або пароль");
        }

        // Create session
        String sessionToken = generateSessionToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_DURATION_HOURS);

        UserSession session = new UserSession(user.getUserId(), sessionToken, expiresAt);
        sessionRepository.save(session);

        return sessionToken;
    }

    /**
     * Logout user by invalidating session
     * @param sessionToken session token
     * @return true if logout successful
     */
    public boolean logout(String sessionToken) {
        return sessionRepository.invalidateSession(sessionToken);
    }

    /**
     * Logout user from all devices
     * @param userId user ID
     * @return true if logout successful
     */
    public boolean logoutAll(Long userId) {
        return sessionRepository.invalidateAllUserSessions(userId);
    }

    /**
     * Validate session token
     * @param sessionToken session token
     * @return user if session is valid, empty otherwise
     */
    public Optional<User> validateSession(String sessionToken) {
        Optional<UserSession> sessionOptional = sessionRepository.findByToken(sessionToken);

        if (sessionOptional.isEmpty()) {
            return Optional.empty();
        }

        UserSession session = sessionOptional.get();

        // Check if session is expired
        if (session.isExpired()) {
            sessionRepository.invalidateSession(sessionToken);
            return Optional.empty();
        }

        // Get user
        return userRepository.findById(session.getUserId());
    }

    /**
     * Change user password
     * @param userId user ID
     * @param oldPassword old password
     * @param newPassword new password
     * @return true if password changed successfully
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Користувача не знайдено");
        }

        User user = userOptional.get();

        // Verify old password
        if (!verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Невірний старий пароль");
        }

        // Validate new password
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Новий пароль повинен містити мінімум 6 символів");
        }

        // Hash and update password
        String newPasswordHash = hashPassword(newPassword);
        boolean updated = userRepository.updatePassword(userId, newPasswordHash);

        // Invalidate all sessions to force re-login
        if (updated) {
            sessionRepository.invalidateAllUserSessions(userId);
        }

        return updated;
    }

    /**
     * Hash password using SHA-256
     * In production, use BCrypt or Argon2
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Verify password against hash
     */
    private boolean verifyPassword(String password, String passwordHash) {
        String hashedInput = hashPassword(password);
        return hashedInput.equals(passwordHash);
    }

    /**
     * Generate secure random session token
     */
    private String generateSessionToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    /**
     * Validate password strength
     */
    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    /**
     * Check if user has specific role
     */
    public boolean hasRole(User user, UserRole role) {
        return user != null && user.getRole() == role;
    }

    /**
     * Check if user is admin
     */
    public boolean isAdmin(User user) {
        return hasRole(user, UserRole.ADMIN);
    }

    /**
     * Check if user is client
     */
    public boolean isClient(User user) {
        return hasRole(user, UserRole.CLIENT);
    }
}