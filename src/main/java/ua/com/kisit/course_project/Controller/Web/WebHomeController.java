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

    public WebHomeController(CarService carService,
                             RentalOrderService orderService,
                             UserRepository userRepository) {
        this.carService = carService;
        this.orderService = orderService;
        this.userRepository = userRepository;
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