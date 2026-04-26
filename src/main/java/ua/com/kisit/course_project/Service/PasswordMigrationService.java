package ua.com.kisit.course_project.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Repository.UserRepository;

@Service
public class PasswordMigrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PasswordMigrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Миграция всех пользователей со старого хэширования на BCrypt
     */
    @Transactional
    public void migrateAllPasswords() {
        try {
            // Сначала удаляем дубликаты
            removeDuplicateUsers();
            
            // Затем выполняем миграцию
            List<User> users = userRepository.findAll();
            
            for (User user : users) {
                try {
                    // Проверяем, является ли пароль старым форматом (не BCrypt)
                    if (isOldFormatPassword(user.getPasswordHash())) {
                        // Установить временный пароль для всех пользователей со старыми хэшами
                        String tempPassword = "temp123"; // Временный пароль
                        String newHash = passwordEncoder.encode(tempPassword);
                        user.setPasswordHash(newHash);
                        userRepository.save(user);
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка обработки пользователя " + user.getEmail() + ": " + e.getMessage());
                    // Продолжаем обработку остальных пользователей
                }
            }
        } catch (Exception e) {
            System.err.println("Общая ошибка миграции: " + e.getMessage());
            throw new RuntimeException("Ошибка миграции паролей: " + e.getMessage(), e);
        }
    }

    /**
     * Удаление дубликатов пользователей
     */
    @Transactional
    private void removeDuplicateUsers() {
        List<User> users = userRepository.findAll();
        
        // Группируем пользователей по email
        Map<String, List<User>> usersByEmail = users.stream()
            .collect(Collectors.groupingBy(User::getEmail));
        
        // Удаляем дубликаты, оставляя только первого пользователя
        for (Map.Entry<String, List<User>> entry : usersByEmail.entrySet()) {
            List<User> userList = entry.getValue();
            if (userList.size() > 1) {
                // Удаляем всех кроме первого (используем deleteById)
                for (int i = 1; i < userList.size(); i++) {
                    userRepository.deleteById(userList.get(i).getUserId());
                }
            }
        }
    }

    /**
     * Проверяет, является ли хэш старого формата (SHA-256 Base64)
     */
    private boolean isOldFormatPassword(String passwordHash) {
        // BCrypt хэши обычно начинаются с $2a$, $2b$ или $2y$
        return passwordHash != null && 
               !passwordHash.startsWith("$2a$") && 
               !passwordHash.startsWith("$2b$") && 
               !passwordHash.startsWith("$2y$");
    }

    /**
     * Сброс всех паролей и отправка уведомлений пользователям
     */
    @Transactional
    public void resetAllPasswordsAndNotifyUsers() {
        try {
            // Сначала удаляем дубликаты
            removeDuplicateUsers();
            
            List<User> users = userRepository.findAll();
            
            for (User user : users) {
                try {
                    // Генерируем временный пароль
                    String tempPassword = generateTemporaryPassword();
                    String hashedPassword = passwordEncoder.encode(tempPassword);
                    user.setPasswordHash(hashedPassword);
                    userRepository.save(user);
                    
                    // Здесь можно добавить отправку email с временным паролем
                    sendPasswordResetEmail(user.getEmail(), tempPassword);
                } catch (Exception e) {
                    System.err.println("Ошибка обработки пользователя " + user.getEmail() + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Общая ошибка сброса паролей: " + e.getMessage());
            throw new RuntimeException("Ошибка сброса паролей: " + e.getMessage(), e);
        }
    }

    private String generateTemporaryPassword() {
        // Генерируем временный пароль
        return "temp" + Math.abs(System.currentTimeMillis() % 1000000);
    }

    private void sendPasswordResetEmail(String email, String tempPassword) {
        // Реализуйте отправку email с временным паролем
        System.out.println("Отправка временного пароля на " + email + ": " + tempPassword);
    }
}