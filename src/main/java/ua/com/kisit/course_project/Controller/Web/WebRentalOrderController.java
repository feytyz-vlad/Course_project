package ua.com.kisit.course_project.Controller.Web;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Client;
import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Service.CarService;
import ua.com.kisit.course_project.Service.ClientService;
import ua.com.kisit.course_project.Service.RentalOrderService;

/**
 * Web Controller for Rental Orders
 */
@Controller
@RequestMapping("/orders")
public class WebRentalOrderController {

    private final RentalOrderService orderService;
    private final CarService carService;
    private final ClientService clientService;

    public WebRentalOrderController(RentalOrderService orderService,
                                    CarService carService,
                                    ClientService clientService) {
        this.orderService = orderService;
        this.carService = carService;
        this.clientService = clientService;
    }

    /**
     * Show all orders (for admin)
     */
    @GetMapping
    public String listOrders(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        UserRole userRole = (UserRole) session.getAttribute("userRole");

        if (userRole == UserRole.ADMIN) {
            List<RentalOrder> orders = orderService.getAllOrders();
            model.addAttribute("orders", orders);
            return "orders/admin-list";
        } else {
            // For clients, show their orders
            Optional<Client> client = clientService.getClientByUserId(userId);
            if (client.isPresent()) {
                List<RentalOrder> orders = orderService.getClientOrders(client.get().getClientId());
                model.addAttribute("orders", orders);
                return "orders/client-list";
            }
        }

        return "redirect:/";
    }

    /**
     * Show create order form
     */
    @GetMapping("/create")
    public String showCreateForm(@RequestParam Long carId, Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        Optional<Car> car = carService.getCarById(carId);
        if (car.isEmpty()) {
            return "redirect:/cars";
        }

        model.addAttribute("car", car.get());
        return "orders/create";
    }

    /**
     * Create new order
     */
    @PostMapping("/create")
    public String createOrder(@RequestParam Long carId,
                              @RequestParam String startDate,
                              @RequestParam String endDate,
                              @RequestParam(required = false) String additionalNotes, // Добавлено
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/auth/login";
        }

        try {
            Optional<Client> client = clientService.getClientByUserId(userId);
            if (client.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Клієнт не знайдений");
                return "redirect:/cars";
            }

            RentalOrder order = orderService.createOrder(
                    client.get().getClientId(),
                    carId,
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate)
            );

            redirectAttributes.addFlashAttribute("success", "Замовлення створено успішно!");
            return "redirect:/orders";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cars/" + carId;
        }
    }

    /**
     * Approve order (admin only)
     */
    @PostMapping("/{id}/approve")
    public String approveOrder(@PathVariable Long id,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        if (userRole != UserRole.ADMIN) {
            return "redirect:/";
        }

        try {
            orderService.approveOrder(id);
            redirectAttributes.addFlashAttribute("success", "Замовлення затверджено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/orders";
    }

    /**
     * Reject order (admin only)
     */
    @PostMapping("/{id}/reject")
    public String rejectOrder(@PathVariable Long id,
                              @RequestParam String reason,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        if (userRole != UserRole.ADMIN) {
            return "redirect:/";
        }

        try {
            orderService.rejectOrder(id, reason);
            redirectAttributes.addFlashAttribute("success", "Замовлення відхилено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/orders";
    }

    /**
     * Complete order (admin only)
     */
    @PostMapping("/{id}/complete")
    public String completeOrder(@PathVariable Long id,
                                @RequestParam String actualReturnDate,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        if (userRole != UserRole.ADMIN) {
            return "redirect:/";
        }

        try {
            orderService.completeOrder(id, LocalDate.parse(actualReturnDate));
            redirectAttributes.addFlashAttribute("success", "Замовлення завершено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/orders";
    }

    /**
     * Show pending orders (admin only)
     */
    @GetMapping("/pending")
    public String pendingOrders(HttpSession session, Model model) {
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        if (userRole != UserRole.ADMIN) {
            return "redirect:/";
        }

        List<RentalOrder> orders = orderService.getPendingOrders();
        model.addAttribute("orders", orders);
        return "orders/pending";
    }
}