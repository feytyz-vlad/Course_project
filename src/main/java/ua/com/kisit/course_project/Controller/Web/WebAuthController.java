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

    public static final String ATTR_ERROR = "error";
    public static final String ATTR_SUCCESS = "success";
    public static final String ANONYMOUS_USER = "anonymousUser";

    private static final String REDIRECT_AUTH_REGISTER = "redirect:/auth/register";
    private static final String REDIRECT_AUTH_LOGIN = "redirect:/auth/login";
    private static final String REDIRECT_PROFILE = "redirect:/profile";
    private static final String REDIRECT_PROFILE_COMPLETE = "redirect:/profile/complete";
    private static final String REDIRECT_PROFILE_CHANGE_PASSWORD = "redirect:/profile/change-password";

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
            model.addAttribute(ATTR_ERROR, "Невірний email або пароль");
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
                redirectAttributes.addFlashAttribute(ATTR_ERROR, "Паролі не співпадають");
                return REDIRECT_AUTH_REGISTER;
            }
            if (password.length() < 6) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR, "Пароль мінімум 6 символів");
                return REDIRECT_AUTH_REGISTER;
            }
            authService.register(email, password, UserRole.CLIENT);
            redirectAttributes.addFlashAttribute(ATTR_SUCCESS, "Реєстрація успішна! Тепер увійдіть.");
            return REDIRECT_AUTH_LOGIN;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, e.getMessage());
            return REDIRECT_AUTH_REGISTER;
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || ANONYMOUS_USER.equals(auth.getPrincipal())) {
            return REDIRECT_AUTH_LOGIN;
        }
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) {
            return REDIRECT_AUTH_LOGIN;
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
            return REDIRECT_AUTH_LOGIN;
        }

        try {
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR, "Нові паролі не співпадають");
                return REDIRECT_PROFILE_CHANGE_PASSWORD;
            }
            Optional<User> userOpt = userRepository.findByEmail(auth.getName());
            if (userOpt.isEmpty()) return REDIRECT_AUTH_LOGIN;
            
            authService.changePassword(userOpt.get().getUserId(), oldPassword, newPassword);
            
            redirectAttributes.addFlashAttribute(ATTR_SUCCESS, "Пароль змінено успішно!");
            return REDIRECT_PROFILE;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, e.getMessage());
        }
        return REDIRECT_PROFILE_CHANGE_PASSWORD;
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
        if (auth == null || !auth.isAuthenticated() || ANONYMOUS_USER.equals(auth.getPrincipal())) {
            return REDIRECT_AUTH_LOGIN;
        }
        try {
            Optional<User> userOpt = userRepository.findByEmail(auth.getName());
            if (userOpt.isEmpty()) return REDIRECT_AUTH_LOGIN;
            
            // Validation
            if (fio.length() > 255) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR, "ПІБ не може перевищувати 255 символів");
                return REDIRECT_PROFILE_COMPLETE;
            }
            if (!phone.matches("^\\+380(-?\\d){9}$")) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR, "Телефон має бути у форматі +380XXXXXXXXX або +380-XX-XXX-XX-XX");
                return REDIRECT_PROFILE_COMPLETE;
            }
            if (!rnokpp.matches("^\\d{10}$")) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR, "РНОКПП має містити рівно 10 цифр");
                return REDIRECT_PROFILE_COMPLETE;
            }

            clientService.createClientProfile(userOpt.get().getUserId(), fio, "", phone, driverLicense, rnokpp);
            redirectAttributes.addFlashAttribute(ATTR_SUCCESS, "Профіль успішно збережено!");
            return REDIRECT_PROFILE;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, e.getMessage());
            return REDIRECT_PROFILE_COMPLETE;
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
        if (auth == null || !auth.isAuthenticated() || ANONYMOUS_USER.equals(auth.getPrincipal())) {
            return REDIRECT_AUTH_LOGIN;
        }
        try {
            Optional<User> userOpt = userRepository.findByEmail(auth.getName());
            if (userOpt.isEmpty()) return REDIRECT_AUTH_LOGIN;
            
            Optional<ua.com.kisit.course_project.Entity.Client> clientOpt = clientService.getClientByUserId(userOpt.get().getUserId());
            if (clientOpt.isEmpty()) {
                // If the profile doesn't exist yet, simply call completeProfile logic
                clientService.createClientProfile(userOpt.get().getUserId(), fio, "", phone, driverLicense, rnokpp);
                redirectAttributes.addFlashAttribute(ATTR_SUCCESS, "Профіль успішно збережено!");
                return REDIRECT_PROFILE;
            }
            
            // Validation
            if (fio.length() > 255) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR, "ПІБ не може перевищувати 255 символів");
                return REDIRECT_PROFILE;
            }
            if (!phone.matches("^\\+380(-?\\d){9}$")) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR, "Телефон має бути у форматі +380XXXXXXXXX або +380-XX-XXX-XX-XX");
                return REDIRECT_PROFILE;
            }
            if (!rnokpp.matches("^\\d{10}$")) {
                redirectAttributes.addFlashAttribute(ATTR_ERROR, "РНОКПП має містити рівно 10 цифр");
                return REDIRECT_PROFILE;
            }

            ua.com.kisit.course_project.Entity.Client client = clientOpt.get();
            client.setFirstName(fio);
            client.setPhone(phone);
            client.setDriverLicenseNumber(driverLicense);
            client.setRnokpp(rnokpp);
            
            clientService.updateClient(client);
            
            redirectAttributes.addFlashAttribute(ATTR_SUCCESS, "Профіль успішно оновлено!");
            return REDIRECT_PROFILE;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(ATTR_ERROR, e.getMessage());
            return REDIRECT_PROFILE;
        }
    }
}