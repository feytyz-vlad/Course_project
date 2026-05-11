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
import ua.com.kisit.course_project.Service.RentalOrderService;
import ua.com.kisit.course_project.Controller.Web.WebAuthController;
import ua.com.kisit.course_project.Controller.Web.WebCarController;

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
    private static final String ATTR_USERS = "users";
    private static final String ATTR_ROLES = "roles";

    private final RentalOrderService orderService;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminController(RentalOrderService orderService,
                           CarRepository carRepository,
                           UserRepository userRepository,
                           AuditLogRepository auditLogRepository) {
        this.orderService = orderService;
        this.carRepository = carRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute(WebCarController.ATTR_TITLE, "Панель адміністратора");
        model.addAttribute(ATTR_PENDING_ORDERS, orderService.getPendingOrders().size());
        model.addAttribute(ATTR_TOTAL_CARS, carRepository.countAll());
        model.addAttribute(ATTR_TOTAL_USERS, userRepository.count());
        model.addAttribute(ATTR_RECENT_LOGS, auditLogRepository.findTop10ByOrderByTimestampDesc());
        return VIEW_DASHBOARD;
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
