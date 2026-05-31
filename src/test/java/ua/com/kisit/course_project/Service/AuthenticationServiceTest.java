package ua.com.kisit.course_project.Service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void register_Success() {
        String email = "test@example.com";
        String password = "password123";
        String encodedHash = "$2a$10$hashedpassword";
        User user = new User(email, encodedHash, UserRole.CLIENT);

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn(encodedHash);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = authenticationService.register(email, password, UserRole.CLIENT);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(encodedHash, result.getPasswordHash());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void register_ThrowsException_WhenEmailExists() {
        String email = "test@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.register(email, "password123", UserRole.CLIENT);
        });

        assertEquals("Користувач з таким email вже існує", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ThrowsException_WhenInvalidEmail() {
        String email = "invalid-email";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.register(email, "password123", UserRole.CLIENT);
        });

        assertEquals("Невірний формат email", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ThrowsException_WhenShortPassword() {
        String email = "test@example.com";

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.register(email, "123", UserRole.CLIENT);
        });

        assertEquals("Пароль повинен містити мінімум 6 символів", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePassword_Success() {
        Long userId = 1L;
        String oldPassword = "oldPassword123";
        String newPassword = "newPassword123";
        String oldHash = "$2a$10$oldhashedpassword";
        String newHash = "$2a$10$newhashedpassword";

        User user = new User("test@example.com", oldHash, UserRole.CLIENT);
        user.setUserId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, oldHash)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(newHash);
        when(userRepository.updatePassword(userId, newHash)).thenReturn(1);

        boolean result = authenticationService.changePassword(userId, oldPassword, newPassword);

        assertTrue(result);
        verify(userRepository, times(1)).updatePassword(userId, newHash);
    }

    @Test
    void changePassword_ThrowsException_WhenUserNotFound() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.changePassword(userId, "old", "newPassword");
        });

        assertEquals("Користувача не знайдено", exception.getMessage());
    }

    @Test
    void changePassword_ThrowsException_WhenOldPasswordIncorrect() {
        Long userId = 1L;
        String oldPassword = "wrongPassword";
        String oldHash = "$2a$10$oldhashedpassword";
        User user = new User("test@example.com", oldHash, UserRole.CLIENT);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(oldPassword, oldHash)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authenticationService.changePassword(userId, oldPassword, "newPassword123");
        });

        assertEquals("Невірний старий пароль", exception.getMessage());
    }

    @Test
    void checkPassword_BCrypt() {
        String raw = "password";
        String encoded = "$2a$10$something";

        when(passwordEncoder.matches(raw, encoded)).thenReturn(true);

        assertTrue(authenticationService.checkPassword(raw, encoded));
    }

    @Test
    void checkPassword_SHA256() {
        String raw = "password123";
        // SHA-256 base64 for "password123"
        // MessageDigest digest = MessageDigest.getInstance("SHA-256");
        // byte[] hash = digest.digest("password123".getBytes());
        // Base64.getEncoder().encodeToString(hash) -> "YQ==" or whatever, we can just hash it or test with pre-calculated value
        // "password123" SHA-256 hash in Base64:
        // Let's compute Base64(SHA256("password123")):
        // SHA-256 of "password123" is: ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f
        // Base64 of that is: 75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8=
        String sha256Encoded = "75K3eLr+dx6JJFuJ7LwIpEpOFmwGZZkRiB84PURz6U8=";

        assertTrue(authenticationService.checkPassword(raw, sha256Encoded));
    }

    @Test
    void roleChecks() {
        User clientUser = new User("test@example.com", "hash", UserRole.CLIENT);
        User adminUser = new User("admin@example.com", "hash", UserRole.ADMIN);

        assertTrue(authenticationService.isClient(clientUser));
        assertFalse(authenticationService.isAdmin(clientUser));

        assertTrue(authenticationService.isAdmin(adminUser));
        assertFalse(authenticationService.isClient(adminUser));
    }
}
