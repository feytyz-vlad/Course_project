package ua.com.kisit.course_project.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Реєстрація нового користувача
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

        String passwordHash = passwordEncoder.encode(password);
        User user = new User(email, passwordHash, role);
        return userRepository.save(user);
    }

    /**
     * Зміна пароля користувача
     */
    @Transactional
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Користувача не знайдено");
        }

        User user = userOptional.get();

        // Проверяем старый пароль с поддержкой обоих форматов
        if (!checkPassword(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Невірний старий пароль");
        }

        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Новий пароль повинен містити мінімум 6 символів");
        }

        // При смене пароля всегда используем BCrypt
        String newPasswordHash = passwordEncoder.encode(newPassword);
        int updatedRows = userRepository.updatePassword(userId, newPasswordHash);
        return updatedRows > 0;
    }

    /**
     * Проверка пароля с поддержкой обоих форматов (BCrypt и SHA-256)
     */
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        // Если пароль в формате BCrypt
        if (encodedPassword.startsWith("$2a$") || 
            encodedPassword.startsWith("$2b$") || 
            encodedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }
        // Если пароль в старом формате SHA-256
        else {
            String sha256Hash = hashPasswordSHA256(rawPassword);
            boolean matches = sha256Hash.equals(encodedPassword);
            return matches;
        }
    }

    /**
     * Хэширование пароля с использованием SHA-256 (старый метод)
     */
    private String hashPasswordSHA256(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
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

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
}