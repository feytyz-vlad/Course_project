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
    private final ua.com.kisit.course_project.Repository.ClientRepository clientRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public CustomOAuth2UserService(UserRepository userRepository, 
                                   ua.com.kisit.course_project.Repository.ClientRepository clientRepository,
                                   org.springframework.security.crypto.password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isEmpty()) {
            user = registerOAuth2User(email, firstName, lastName);
        } else {
            user = existingUser.get();
        }

        // Створюємо список прав (ролей) на основі ролі з бази даних
        java.util.List<org.springframework.security.core.GrantedAuthority> authorities = 
            java.util.Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // Повертаємо користувача з правильними ролями
        return new org.springframework.security.oauth2.core.user.DefaultOAuth2User(
            authorities, 
            oAuth2User.getAttributes(), 
            "email" // Використовуємо email як основний ідентифікатор (Name)
        );
    }

    private User registerOAuth2User(String email, String firstName, String lastName) {
        // Генерируем случайный пароль для OAuth2 пользователей
        String passwordHash = passwordEncoder.encode(generateRandomPassword());
        User user = new User(email, passwordHash, UserRole.CLIENT);
        User savedUser = userRepository.save(user);

        // Создаем запись клиента
        ua.com.kisit.course_project.Entity.Client client = new ua.com.kisit.course_project.Entity.Client();
        client.setUserId(savedUser.getUserId());
        client.setFirstName(firstName != null ? firstName : "Клієнт");
        client.setLastName(lastName != null ? lastName : "Google");
        client.setCreatedAt(java.time.LocalDateTime.now());
        client.setUpdatedAt(java.time.LocalDateTime.now());
        clientRepository.save(client);
        
        return savedUser;
    }

    private String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return "oauth2-" + bytesToHex(bytes);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}