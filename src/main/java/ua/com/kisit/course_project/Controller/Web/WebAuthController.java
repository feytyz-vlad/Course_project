package ua.com.kisit.course_project.Controller.Web;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Service.AuthenticationService;

@Controller
public class WebAuthController {

    private final AuthenticationService authService;

    public WebAuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @GetMapping({"/login", "/auth/login"})
    public String showLoginPage(HttpSession session) {
        if (session.getAttribute("userId") != null) return "redirect:/";
        return "auth/login";
    }

    @PostMapping({"/login", "/auth/login"})
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
                redirectAttributes.addFlashAttribute("success", "Ласкаво просимо!");
                return user.get().getRole() == UserRole.ADMIN ? "redirect:/cars" : "redirect:/";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Невірний email або пароль");
        }
        return "redirect:/login";
    }

    @GetMapping({"/register", "/auth/register"})
    public String showRegisterPage(HttpSession session) {
        if (session.getAttribute("userId") != null) return "redirect:/";
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
                return "redirect:/register";
            }
            if (password.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "Пароль мінімум 6 символів");
                return "redirect:/register";
            }
            authService.register(email, password, UserRole.CLIENT);
            redirectAttributes.addFlashAttribute("success", "Реєстрація успішна! Тепер увійдіть.");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        String token = (String) session.getAttribute("sessionToken");
        if (token != null) authService.logout(token);
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Ви успішно вийшли");
        return "redirect:/";
    }

    @GetMapping("/profile")
    public String showProfile(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        String token = (String) session.getAttribute("sessionToken");
        Optional<User> user = authService.validateSession(token);
        if (user.isPresent()) {
            model.addAttribute("user", user.get());
            return "auth/profile";
        }
        return "redirect:/login";
    }

    @GetMapping("/profile/change-password")
    public String showChangePasswordPage(HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        return "auth/change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "Нові паролі не співпадають");
                return "redirect:/profile/change-password";
            }
            boolean ok = authService.changePassword(userId, oldPassword, newPassword);
            if (ok) {
                session.invalidate();
                redirectAttributes.addFlashAttribute("success", "Пароль змінено! Увійдіть знову.");
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("error", "Невірний старий пароль");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/profile/change-password";
    }
}