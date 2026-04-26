package ua.com.kisit.course_project.Controller.Web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;
import ua.com.kisit.course_project.Service.AuthenticationService;
import ua.com.kisit.course_project.Service.ClientService;

@Controller
public class WebAuthController {

    private final AuthenticationService authService;
    private final UserRepository userRepository;
    private final ClientService clientService;

    public WebAuthController(AuthenticationService authService, UserRepository userRepository, ClientService clientService) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.clientService = clientService;
    }

    // Spring Security обробляє POST /auth/login автоматично
    @GetMapping({"/login", "/auth/login"})
    public String showLoginPage(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Невірний email або пароль");
        }
        return "auth/login";
    }

    @GetMapping({"/register", "/auth/register"})
    public String showRegisterPage() {
        return "auth/register";
    }

    @PostMapping({"/register", "/auth/register"})
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           RedirectAttributes redirectAttributes) {
        try {
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Паролі не співпадають");
                return "redirect:/auth/register";
            }
            if (password.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Пароль мінімум 6 символів");
                return "redirect:/auth/register";
            }
            authService.register(email, password, UserRole.CLIENT);
            redirectAttributes.addFlashAttribute("success", "Реєстрація успішна! Тепер увійдіть.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) {
            return "redirect:/auth/login";
        }
        model.addAttribute("user", userOpt.get());
        return "auth/profile";
    }

    @GetMapping("/profile/change-password")
    public String showChangePasswordPage() {
        return "auth/change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Нові паролі не співпадають");
                return "redirect:/profile/change-password";
            }
            Optional<User> userOpt = userRepository.findByEmail(auth.getName());
            if (userOpt.isEmpty()) return "redirect:/auth/login";
            
            authService.changePassword(userOpt.get().getUserId(), oldPassword, newPassword);
            
            redirectAttributes.addFlashAttribute("success", "Пароль змінено успішно!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/change-password";
    }

    @GetMapping("/profile/complete")
    public String showCompleteProfilePage() {
        return "auth/complete-profile";
    }

    @PostMapping("/register/complete")
    public String completeProfile(@RequestParam String firstName,
                                  @RequestParam String lastName,
                                  @RequestParam String phone,
                                  @RequestParam String driverLicense,
                                  RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }
        try {
            Optional<User> userOpt = userRepository.findByEmail(auth.getName());
            if (userOpt.isEmpty()) return "redirect:/auth/login";
            
            clientService.createClientProfile(userOpt.get().getUserId(), firstName, lastName, phone, driverLicense);
            redirectAttributes.addFlashAttribute("success", "Профіль успішно збережено!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/complete";
        }
    }
}