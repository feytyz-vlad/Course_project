package ua.com.kisit.course_project.Controller.Web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ua.com.kisit.course_project.Service.PasswordMigrationService;

@Controller
public class PasswordMigrationController {

    private final PasswordMigrationService passwordMigrationService;

    public PasswordMigrationController(PasswordMigrationService passwordMigrationService) {
        this.passwordMigrationService = passwordMigrationService;
    }

    // Временная страница для запуска миграции
    @GetMapping("/migrate-passwords")
    public String showMigrationPage() {
        return "auth/migrate-passwords";
    }

    // Временный эндпоинт для миграции паролей (без авторизации)
    @PostMapping("/migrate-passwords")
    public String migratePasswords(@RequestParam String secretKey,
                                   RedirectAttributes redirectAttributes) {
        // Простой секретный ключ для защиты эндпоинта (замените на свой)
        if (!"admin123".equals(secretKey)) {
            redirectAttributes.addFlashAttribute("error", "Неверный секретный ключ");
            return "redirect:/migrate-passwords";
        }

        try {
            // Мигрируем все пароли в BCrypt
            passwordMigrationService.migrateAllPasswords();
            redirectAttributes.addFlashAttribute("success", "Все пароли успешно мигрированы! Используйте временный пароль 'temp123' для входа.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка миграции: " + e.getMessage());
        }
        
        return "redirect:/migrate-passwords";
    }
}
