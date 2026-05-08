package ua.com.kisit.course_project.Controller.Admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ua.com.kisit.course_project.Annotation.Auditable;
import ua.com.kisit.course_project.Service.PasswordMigrationService;

@Controller("adminActionsController") // явное имя бина, чтобы не конфликтовать
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class AdminController {

    private final PasswordMigrationService passwordMigrationService;

    public AdminController(PasswordMigrationService passwordMigrationService) {
        this.passwordMigrationService = passwordMigrationService;
    }

    // UI endpoints (GET) — теперь в этом контроллере
    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/manage-users")
    public String manageUsers() {
        return "admin/users";
    }

    @GetMapping("/settings")
    public String settings() {
        return "admin/settings";
    }

    // Action endpoints (POST)
    @PostMapping("/migrate-passwords")
    @Auditable(action = "MIGRATE_PASSWORDS")
    public String migratePasswords(RedirectAttributes redirectAttributes) {
        try {
            passwordMigrationService.migrateAllPasswords();
            redirectAttributes.addFlashAttribute("success", "Міграція паролів завершена успішно!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Сталася помилка під час міграції паролів: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/reset-passwords")
    @Auditable(action = "RESET_PASSWORDS")
    public String resetPasswords(RedirectAttributes redirectAttributes) {
        try {
            passwordMigrationService.resetAllPasswordsAndNotifyUsers();
            redirectAttributes.addFlashAttribute("success", "Паролі скинуті, користувачі отримали нові тимчасові паролі на пошту.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Сталася помилка під час скидання паролів: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }
}
