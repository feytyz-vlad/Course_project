package ua.com.kisit.course_project.Service;

import java.security.SecureRandom;
import java.util.Optional;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // Получаем email из данных Google аккаунта
        String email = oAuth2User.getAttribute("email");

        // Проверяем, существует ли пользователь с таким email
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isEmpty()) {
            // Если пользователя нет, создаем нового с ролью CLIENT
            registerOAuth2User(email);
        }

        return oAuth2User;
    }

    private void registerOAuth2User(String email) {
        if (userRepository.existsByEmail(email)) {
            return; // Пользователь уже существует
        }

        // Генерируем случайный пароль для OAuth2 пользователей
        String passwordHash = hashPassword(generateRandomPassword());
        User user = new User(email, passwordHash, UserRole.CLIENT);
        userRepository.save(user);
    }

    private String generateRandomPassword() {
        // Генерируем случайный пароль для OAuth2 пользователей
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return "oauth2-" + bytesToHex(bytes);
    }

    private String hashPassword(String password) {
        // Простое хэширование SHA-256 (в реальном приложении используйте BCrypt)
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return java.util.Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}