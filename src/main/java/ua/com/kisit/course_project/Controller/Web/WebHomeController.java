package ua.com.kisit.course_project.Controller.Web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Service.CarService;
import ua.com.kisit.course_project.Service.ClientService;
import ua.com.kisit.course_project.Service.RentalOrderService;

import java.util.List;

/**
 * Web Controller for Home page
 */
@Controller
public class WebHomeController {

    private final CarService carService;
    private final ClientService clientService;
    private final RentalOrderService orderService;

    public WebHomeController(CarService carService,
                             ClientService clientService,
                             RentalOrderService orderService) {
        this.carService = carService;
        this.clientService = clientService;
        this.orderService = orderService;
    }

    /**
     * Show home page with available cars
     */
    @GetMapping("/")
    public String homePage(HttpSession session, Model model) {
        List<Car> availableCars = carService.getAvailableCars();
        model.addAttribute("cars", availableCars);

        Long userId = (Long) session.getAttribute("userId");
        if (userId != null) {
            UserRole userRole = (UserRole) session.getAttribute("userRole");
            model.addAttribute("userRole", userRole);

            if (userRole == UserRole.ADMIN) {
                model.addAttribute("totalOrders", orderService.getAllOrders().size());
                model.addAttribute("pendingOrders", orderService.getPendingOrders().size());
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