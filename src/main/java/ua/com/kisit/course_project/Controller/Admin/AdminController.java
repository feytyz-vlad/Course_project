package ua.com.kisit.course_project.Controller.Admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.ui.Model;

import ua.com.kisit.course_project.Annotation.Auditable;
import ua.com.kisit.course_project.Service.DamageReportService;
import ua.com.kisit.course_project.Service.PasswordMigrationService;
import ua.com.kisit.course_project.Service.CarService;
import ua.com.kisit.course_project.Service.RentalOrderService;
import ua.com.kisit.course_project.Repository.UserRepository;

@Controller("adminActionsController") // явное имя бина, чтобы не конфликтовать
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
public class AdminController {

    private final PasswordMigrationService passwordMigrationService;
    private final DamageReportService damageReportService;
    private final CarService carService;
    private final RentalOrderService rentalOrderService;
    private final UserRepository userRepository;

    public AdminController(PasswordMigrationService passwordMigrationService, 
                           DamageReportService damageReportService,
                           CarService carService,
                           RentalOrderService rentalOrderService,
                           UserRepository userRepository) {
        this.passwordMigrationService = passwordMigrationService;
        this.damageReportService = damageReportService;
        this.carService = carService;
        this.rentalOrderService = rentalOrderService;
        this.userRepository = userRepository;
    }

    // UI endpoints (GET) — теперь в этом контроллере
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalCars", carService.getTotalCarsCount());
        model.addAttribute("availableCars", carService.getAvailableCarsCount());
        model.addAttribute("totalOrders", rentalOrderService.getAllOrders().size());
        model.addAttribute("pendingOrders", rentalOrderService.getPendingOrders().size());
        model.addAttribute("totalDamages", damageReportService.getAllReports().size());
        model.addAttribute("latestOrders", rentalOrderService.getLatestOrders(5));
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

    @GetMapping("/damages")
    public String damages(Model model) {
        model.addAttribute("reports", damageReportService.getAllReports());
        return "admin/damages";
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
