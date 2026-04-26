package ua.com.kisit.course_project.Controller.Web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Repository.UserRepository;
import ua.com.kisit.course_project.Service.CarService;
import ua.com.kisit.course_project.Service.DamageReportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

@Controller
@RequestMapping("/manager")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class WebManagerController {

    private final DamageReportService damageReportService;
    private final CarService carService;
    private final UserRepository userRepository;

    public WebManagerController(DamageReportService damageReportService, 
                                CarService carService, 
                                UserRepository userRepository) {
        this.damageReportService = damageReportService;
        this.carService = carService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public String managerDashboard(Model model) {
        model.addAttribute("title", "Панель менеджера");
        model.addAttribute("totalCars", carService.getAllCars().size());
        model.addAttribute("availableCars", carService.getAvailableCars().size());
        model.addAttribute("totalDamages", damageReportService.getAllReports().size());
        return "manager/dashboard";
    }

    @GetMapping("/damages")
    public String getDamages(Model model) {
        model.addAttribute("damages", damageReportService.getAllReports());
        model.addAttribute("title", "Пошкодження автомобілів");
        return "manager/damages";
    }

    @GetMapping("/cars/{id}/damage/add")
    public String showDamageForm(@PathVariable Long id, Model model) {
        Optional<Car> carOpt = carService.getCarById(id);
        if (carOpt.isEmpty()) return "redirect:/cars";
        
        model.addAttribute("car", carOpt.get());
        model.addAttribute("title", "Фіксація пошкодження");
        return "manager/damage-form";
    }

    @PostMapping("/cars/{id}/damage")
    public String recordDamage(@PathVariable Long id, 
                               @RequestParam Long orderId,
                               @RequestParam String description,
                               @RequestParam String damageDate,
                               RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> userOpt = userRepository.findByEmail(auth.getName());
            Long userId = userOpt.map(User::getUserId).orElse(0L);

            damageReportService.createReport(
                    orderId,
                    id,
                    description,
                    LocalDate.parse(damageDate),
                    userId
            );
            redirectAttributes.addFlashAttribute("success", "Пошкодження успішно зафіксовано!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка: " + e.getMessage());
        }
        return "redirect:/cars/" + id;
    }

    @PostMapping("/damages/{id}/invoice")
    public String issueInvoice(@PathVariable Long id, 
                               @RequestParam BigDecimal cost, 
                               RedirectAttributes redirectAttributes) {
        try {
            damageReportService.setRepairCost(id, cost);
            redirectAttributes.addFlashAttribute("success", "Рахунок за ремонт виставлено успішно!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка: " + e.getMessage());
        }
        return "redirect:/manager/damages";
    }

    @PostMapping("/damages/{id}/complete")
    public String completeRepair(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            damageReportService.markAsCompleted(id);
            redirectAttributes.addFlashAttribute("success", "Ремонт завершено, авто знову доступне!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Помилка: " + e.getMessage());
        }
        return "redirect:/manager/damages";
    }
}