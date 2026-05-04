package ua.com.kisit.course_project.Controller.Web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpSession;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;
import ua.com.kisit.course_project.Service.CarService;
import ua.com.kisit.course_project.Service.RentalOrderService;

/**
 * Web Controller for Home page
 */
@Controller
public class WebHomeController {

    private final CarService carService;
    private final RentalOrderService orderService;
    private final UserRepository userRepository;
    private final ua.com.kisit.course_project.Service.ClientService clientService;
    private final ua.com.kisit.course_project.Service.DamageReportService damageService;

    public WebHomeController(CarService carService,
                             RentalOrderService orderService,
                             UserRepository userRepository,
                             ua.com.kisit.course_project.Service.ClientService clientService,
                             ua.com.kisit.course_project.Service.DamageReportService damageService) {
        this.carService = carService;
        this.orderService = orderService;
        this.userRepository = userRepository;
        this.clientService = clientService;
        this.damageService = damageService;
    }

    /**
     * Show home page with available cars
     */
    @GetMapping("/")
    public String homePage(HttpSession session, Model model) {
        List<Car> availableCars = carService.getAvailableCars();
        model.addAttribute("cars", availableCars);
        model.addAttribute("totalCars", carService.getAllCars().size());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            Optional<User> userOpt = userRepository.findByEmail(auth.getName());
            if (userOpt.isPresent()) {
                UserRole userRole = userOpt.get().getRole();
                model.addAttribute("userRole", userRole);

                if (userRole == UserRole.ADMIN) {
                    model.addAttribute("totalOrders", orderService.getAllOrders().size());
                    model.addAttribute("pendingOrders", orderService.getPendingOrders().size());
                } else {
                    // Перевірка на неоплачені штрафи для клієнта
                    clientService.getClientByUserId(userOpt.get().getUserId()).ifPresent(client -> {
                        List<ua.com.kisit.course_project.Entity.RentalOrder> clientOrders = orderService.getClientOrders(client.getClientId());
                        java.util.List<ua.com.kisit.course_project.Entity.DamageReport> unpaidDamages = new java.util.ArrayList<>();
                        for (ua.com.kisit.course_project.Entity.RentalOrder order : clientOrders) {
                            damageService.getReportsByOrderId(order.getOrderId()).stream()
                                    .filter(r -> r.getRepairStatus() != ua.com.kisit.course_project.Entity.DamageReport.RepairStatus.PAID)
                                    .forEach(unpaidDamages::add);
                        }
                        model.addAttribute("unpaidDamages", unpaidDamages);
                    });
                }
            }
        }

        return "home";
    }

    /**
     * About page
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }

    /**
     * Contact page
     */
    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}