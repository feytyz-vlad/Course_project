package ua.com.kisit.course_project.Controller.Web;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ua.com.kisit.course_project.Annotation.Auditable;
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

    @Auditable(action = "USER_REGISTER")
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
        Optional<ua.com.kisit.course_project.Entity.Client> clientOpt = clientService.getClientByUserId(userOpt.get().getUserId());
        clientOpt.ifPresent(client -> model.addAttribute("client", client));
        return "auth/profile";
    }

    @GetMapping("/profile/change-password")
    public String showChangePasswordPage() {
        return "auth/change-password";
    }

    @Auditable(action = "CHANGE_PASSWORD")
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

    @Auditable(action = "COMPLETE_PROFILE")
    @PostMapping("/register/complete")
    public String completeProfile(@RequestParam String fio,
                                  @RequestParam String phone,
                                  @RequestParam String driverLicense,
                                  @RequestParam String rnokpp,
                                  RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }
        try {
            Optional<User> userOpt = userRepository.findByEmail(auth.getName());
            if (userOpt.isEmpty()) return "redirect:/auth/login";
            
            // Validation
            if (fio.length() > 255) {
                redirectAttributes.addFlashAttribute("error", "ПІБ не може перевищувати 255 символів");
                return "redirect:/profile/complete";
            }
            if (!phone.matches("^\\+380(-?\\d){9}$")) {
                redirectAttributes.addFlashAttribute("error", "Телефон має бути у форматі +380XXXXXXXXX або +380-XX-XXX-XX-XX");
                return "redirect:/profile/complete";
            }
            if (!rnokpp.matches("^\\d{10}$")) {
                redirectAttributes.addFlashAttribute("error", "РНОКПП має містити рівно 10 цифр");
                return "redirect:/profile/complete";
            }

            clientService.createClientProfile(userOpt.get().getUserId(), fio, "", phone, driverLicense, rnokpp);
            redirectAttributes.addFlashAttribute("success", "Профіль успішно збережено!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile/complete";
        }
    }

    @Auditable(action = "UPDATE_PROFILE")
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String fio,
                                @RequestParam String phone,
                                @RequestParam String driverLicense,
                                @RequestParam String rnokpp,
                                RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }
        try {
            Optional<User> userOpt = userRepository.findByEmail(auth.getName());
            if (userOpt.isEmpty()) return "redirect:/auth/login";
            
            Optional<ua.com.kisit.course_project.Entity.Client> clientOpt = clientService.getClientByUserId(userOpt.get().getUserId());
            if (clientOpt.isEmpty()) {
                // If the profile doesn't exist yet, simply call completeProfile logic
                clientService.createClientProfile(userOpt.get().getUserId(), fio, "", phone, driverLicense, rnokpp);
                redirectAttributes.addFlashAttribute("success", "Профіль успішно збережено!");
                return "redirect:/profile";
            }
            
            // Validation
            if (fio.length() > 255) {
                redirectAttributes.addFlashAttribute("error", "ПІБ не може перевищувати 255 символів");
                return "redirect:/profile";
            }
            if (!phone.matches("^\\+380(-?\\d){9}$")) {
                redirectAttributes.addFlashAttribute("error", "Телефон має бути у форматі +380XXXXXXXXX або +380-XX-XXX-XX-XX");
                return "redirect:/profile";
            }
            if (!rnokpp.matches("^\\d{10}$")) {
                redirectAttributes.addFlashAttribute("error", "РНОКПП має містити рівно 10 цифр");
                return "redirect:/profile";
            }

            ua.com.kisit.course_project.Entity.Client client = clientOpt.get();
            client.setFirstName(fio);
            client.setPhone(phone);
            client.setDriverLicenseNumber(driverLicense);
            client.setRnokpp(rnokpp);
            
            clientService.updateClient(client);
            
            redirectAttributes.addFlashAttribute("success", "Профіль успішно оновлено!");
            return "redirect:/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile";
        }
    }
}