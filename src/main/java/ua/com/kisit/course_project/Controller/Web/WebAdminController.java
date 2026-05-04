package ua.com.kisit.course_project.Controller.Web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;
import ua.com.kisit.course_project.Service.CarService;
import ua.com.kisit.course_project.Service.DamageReportService;
import ua.com.kisit.course_project.Service.RentalOrderService;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class WebAdminController {

    private final UserRepository userRepository;
    private final CarService carService;
    private final RentalOrderService orderService;
    private final DamageReportService damageService;

    public WebAdminController(UserRepository userRepository, 
                              CarService carService, 
                              RentalOrderService orderService,
                              DamageReportService damageService) {
        this.userRepository = userRepository;
        this.carService = carService;
        this.orderService = orderService;
        this.damageService = damageService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("title", "Панель управління");
        model.addAttribute("totalUsers", userRepository.findAll().size());
        model.addAttribute("totalCars", carService.getAllCars().size());
        model.addAttribute("availableCars", carService.getAvailableCars().size());
        model.addAttribute("totalOrders", orderService.getAllOrders().size());
        model.addAttribute("pendingOrders", orderService.getPendingOrders().size());
        model.addAttribute("totalDamages", damageService.getAllReports().size());
        model.addAttribute("latestOrders", orderService.getLatestOrders(5));
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("roles", UserRole.values());
        model.addAttribute("title", "Керування користувачами");
        return "admin/users";
    }

    @GetMapping("/damages")
    public String getDamages(Model model) {
        model.addAttribute("reports", damageService.getAllReports());
        model.addAttribute("title", "Звіти про пошкодження");
        return "admin/damages";
    }

    @PostMapping("/users/{id}/role")
    public String changeUserRole(@PathVariable Long id, @RequestParam String role, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setRole(UserRole.valueOf(role));
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("success", "Роль користувача оновлено!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-active")
    public String toggleUserActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setActive(!user.isActive());
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("success", "Статус користувача змінено!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Користувача видалено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}