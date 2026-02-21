package ua.com.kisit.course_project.Service;

import org.springframework.stereotype.Service;
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
@Service
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
     */
    public User register(String email, String password, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Користувач з таким email вже існує");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Невірний формат email");
        }
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Пароль повинен містити мінімум 6 символів");
        }

        String passwordHash = hashPassword(password);
        User user = new User(email, passwordHash, role);
        return userRepository.save(user);
    }

    /**
     * Login user and create session
     */
    public String login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Невірний email або пароль");
        }

        User user = userOptional.get();

        if (!user.isActive()) {
            throw new IllegalStateException("Обліковий запис деактивовано");
        }

        if (!verifyPassword(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Невірний email або пароль");
        }

        String sessionToken = generateSessionToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(SESSION_DURATION_HOURS);

        UserSession session = new UserSession(user.getUserId(), sessionToken, expiresAt);
        sessionRepository.save(session);

        return sessionToken;
    }

    /**
     * Logout user by invalidating session
     */
    public boolean logout(String sessionToken) {
        return sessionRepository.invalidateSession(sessionToken);
    }

    /**
     * Logout user from all devices
     */
    public boolean logoutAll(Long userId) {
        return sessionRepository.invalidateAllUserSessions(userId);
    }

    /**
     * Validate session token and return user
     */
    public Optional<User> validateSession(String sessionToken) {
        Optional<UserSession> sessionOptional = sessionRepository.findByToken(sessionToken);

        if (sessionOptional.isEmpty()) {
            return Optional.empty();
        }

        UserSession session = sessionOptional.get();

        if (session.isExpired()) {
            sessionRepository.invalidateSession(sessionToken);
            return Optional.empty();
        }

        return userRepository.findById(session.getUserId());
    }

    /**
     * Change user password
     */
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Користувача не знайдено");
        }

        User user = userOptional.get();

        if (!verifyPassword(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Невірний старий пароль");
        }

        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Новий пароль повинен містити мінімум 6 символів");
        }

        String newPasswordHash = hashPassword(newPassword);
        boolean updated = userRepository.updatePassword(userId, newPasswordHash);

        if (updated) {
            sessionRepository.invalidateAllUserSessions(userId);
        }

        return updated;
    }

    // ===== Helper methods =====

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean verifyPassword(String password, String passwordHash) {
        return hashPassword(password).equals(passwordHash);
    }

    private String generateSessionToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }

    public boolean hasRole(User user, UserRole role) {
        return user != null && user.getRole() == role;
    }

    public boolean isAdmin(User user) {
        return hasRole(user, UserRole.ADMIN);
    }

    public boolean isClient(User user) {
        return hasRole(user, UserRole.CLIENT);
    }
}