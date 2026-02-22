package ua.com.kisit.course_project.Controller.Web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Service.AuthenticationService;

import java.util.Optional;

/**
 * Web Controller for Authentication
 */
@Controller
@RequestMapping("/auth")
public class WebAuthController {

    private final AuthenticationService authService;

    public WebAuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    /**
     * Show login page
     */
    @GetMapping("/login")
    public String showLoginPage() {
        return "auth/login";
    }

    /**
     * Handle login
     */
    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            String sessionToken = authService.login(email, password);

            Optional<User> user = authService.validateSession(sessionToken);
            if (user.isPresent()) {
                session.setAttribute("sessionToken", sessionToken);
                session.setAttribute("userId", user.get().getUserId());
                session.setAttribute("userEmail", user.get().getEmail());
                session.setAttribute("userRole", user.get().getRole());

                redirectAttributes.addFlashAttribute("success", "Вітаємо!");

                if (user.get().getRole() == UserRole.ADMIN) {
                    return "redirect:/admin/dashboard";
                } else {
                    return "redirect:/";
                }
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/auth/login";
    }

    /**
     * Show registration page
     */
    @GetMapping("/register")
    public String showRegisterPage() {
        return "auth/register";
    }

    /**
     * Handle registration
     */
    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           RedirectAttributes redirectAttributes) {
        try {
            if (!password.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Паролі не співпадають");
                return "redirect:/auth/register";
            }

            User user = authService.register(email, password, UserRole.CLIENT);

            if (user != null) {
                redirectAttributes.addFlashAttribute("success",
                        "Реєстрація успішна! Тепер ви можете увійти.");
                return "redirect:/auth/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/auth/register";
    }

    /**
     * Handle logout
     */
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        String sessionToken = (String) session.getAttribute("sessionToken");

        if (sessionToken != null) {
            authService.logout(sessionToken);
        }

        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Ви успішно вийшли");
        return "redirect:/";
    }

    /**
     * Show profile page
     */
    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        String sessionToken = (String) session.getAttribute("sessionToken");
        Optional<User> user = authService.validateSession(sessionToken);

        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "auth/profile";
        }

        return "redirect:/auth/login";
    }

    /**
     * Show change password page
     */
    @GetMapping("/change-password")
    public String showChangePasswordPage(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }
        return "auth/change-password";
    }

    /**
     * Handle password change
     */
    @PostMapping("/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Нові паролі не співпадають");
                return "redirect:/auth/change-password";
            }

            boolean success = authService.changePassword(userId, oldPassword, newPassword);

            if (success) {
                session.invalidate();
                redirectAttributes.addFlashAttribute("success",
                        "Пароль змінено! Увійдіть з новим паролем.");
                return "redirect:/auth/login";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/auth/change-password";
    }
}