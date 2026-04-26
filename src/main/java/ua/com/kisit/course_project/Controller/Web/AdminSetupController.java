package ua.com.kisit.course_project.Controller.Web;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;

@Controller
public class AdminSetupController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminSetupController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Временный endpoint для создания администратора (удалите после использования)
    @GetMapping("/setup/admin")
    public String showAdminSetupPage() {
        return "auth/setup-user"; // Переименовал в более общий
    }

    @PostMapping("/setup/admin")
    public String createAdmin(@RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String role,
                              RedirectAttributes redirectAttributes) {
        try {
            // Проверяем, существует ли уже пользователь
            if (userRepository.existsByEmail(email)) {
                redirectAttributes.addFlashAttribute("error", "Користувач з таким email вже існує");
                return "redirect:/setup/admin";
            }

            // Создаем нового пользователя
            String passwordHash = passwordEncoder.encode(password);
            User user = new User(email, passwordHash, UserRole.valueOf(role));
            userRepository.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Користувача з роллю " + role + " створено успішно!");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка створення: " + e.getMessage());
            return "redirect:/setup/admin";
        }
    }
}
