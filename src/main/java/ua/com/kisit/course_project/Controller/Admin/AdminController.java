package ua.com.kisit.course_project.Controller.Admin;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.AuditLogRepository;
import ua.com.kisit.course_project.Repository.CarRepository;
import ua.com.kisit.course_project.Repository.UserRepository;
import ua.com.kisit.course_project.Controller.Web.WebAuthController;
import ua.com.kisit.course_project.Controller.Web.WebCarController;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Service.DamageReportService;
import ua.com.kisit.course_project.Service.RentalOrderService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final String VIEW_DASHBOARD = "admin/dashboard";
    private static final String VIEW_USERS = "admin/users";
    private static final String REDIRECT_USERS = "redirect:/admin/users";

    private static final String ATTR_PENDING_ORDERS = "pendingOrders";
    private static final String ATTR_TOTAL_CARS = "totalCars";
    private static final String ATTR_TOTAL_USERS = "totalUsers";
    private static final String ATTR_RECENT_LOGS = "recentLogs";
    private static final String ATTR_LATEST_ORDERS = "latestOrders";
    private static final String ATTR_AVAILABLE_CARS = "availableCars";
    private static final String ATTR_TOTAL_ORDERS = "totalOrders";
    private static final String ATTR_TOTAL_DAMAGES = "totalDamages";
    private static final String ATTR_USERS = "users";
    private static final String ATTR_ROLES = "roles";
    private static final String ATTR_REPORTS = "reports";

    private final RentalOrderService orderService;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final DamageReportService damageService;

    public AdminController(RentalOrderService orderService,
                           CarRepository carRepository,
                           UserRepository userRepository,
                           AuditLogRepository auditLogRepository,
                           DamageReportService damageService) {
        this.orderService = orderService;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.damageService = damageService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute(WebCarController.ATTR_TITLE, "Панель адміністратора");
        model.addAttribute(ATTR_PENDING_ORDERS, orderService.getPendingOrders().size());
        model.addAttribute(ATTR_TOTAL_CARS, carRepository.countAll());
        model.addAttribute(ATTR_AVAILABLE_CARS, carRepository.countAvailable());
        model.addAttribute(ATTR_TOTAL_ORDERS, orderService.getAllOrders().size());
        model.addAttribute(ATTR_TOTAL_USERS, userRepository.count());
        model.addAttribute(ATTR_TOTAL_DAMAGES, damageService.getAllReports().size());
        model.addAttribute(ATTR_RECENT_LOGS, auditLogRepository.findTop10ByOrderByCreatedAtDesc());
        model.addAttribute(ATTR_LATEST_ORDERS, orderService.getLatestOrders(5));
        model.addAttribute("ukLocale", new java.util.Locale("uk", "UA"));
        return VIEW_DASHBOARD;
    }

    @GetMapping("/damages")
    public String manageDamages(Model model) {
        model.addAttribute(WebCarController.ATTR_TITLE, "Контроль пошкоджень");
        model.addAttribute(ATTR_REPORTS, damageService.getAllReports());
        model.addAttribute("ukLocale", new java.util.Locale("uk", "UA"));
        return "admin/damages";
    }

    @GetMapping("/users")
    public String manageUsers(Model model) {
        model.addAttribute(WebCarController.ATTR_TITLE, "Керування користувачами");
        model.addAttribute(ATTR_USERS, userRepository.findAll());
        model.addAttribute(ATTR_ROLES, UserRole.values());
        return VIEW_USERS;
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable Long id, @RequestParam UserRole role, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setRole(role);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_SUCCESS, "Роль користувача оновлена");
        } else {
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, "Користувача не знайдено");
        }
        return REDIRECT_USERS;
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleUserActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setActive(!user.isActive());
            userRepository.save(user);
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_SUCCESS, "Статус користувача змінено");
        } else {
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, "Користувача не знайдено");
        }
        return REDIRECT_USERS;
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_SUCCESS, "Користувача видалено");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, "Помилка при видаленні: " + e.getMessage());
        }
        return REDIRECT_USERS;
    }
}
