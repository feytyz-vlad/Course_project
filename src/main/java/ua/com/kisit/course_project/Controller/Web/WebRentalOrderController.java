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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Client;
import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Repository.UserRepository;
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
    private final UserRepository userRepository;

    public WebRentalOrderController(RentalOrderService orderService,
                                    CarService carService,
                                    ClientService clientService,
                                    UserRepository userRepository) {
        this.orderService = orderService;
        this.carService = carService;
        this.clientService = clientService;
        this.userRepository = userRepository;
    }

    /**
     * Show all orders (for admin)
     */
    @GetMapping
    public String listOrders(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return "redirect:/auth/login";
        
        Long userId = userOpt.get().getUserId();
        UserRole userRole = userOpt.get().getRole();

        if (userRole == UserRole.ADMIN) {
            List<RentalOrder> orders = orderService.getAllOrders();
            model.addAttribute("orders", orders);
            return "orders/list";
        } else {
            // For clients, show their orders
            Optional<Client> client = clientService.getClientByUserId(userId);
            if (client.isPresent()) {
                List<RentalOrder> orders = orderService.getClientOrders(client.get().getClientId());
                model.addAttribute("orders", orders);
                model.addAttribute("title", "Мої замовлення");
                return "orders/list";
            } else {
                return "redirect:/profile/complete";
            }
        }
    }

    /**
     * Show create order form
     */
    @GetMapping("/create")
    public String showCreateForm(@RequestParam Long carId, Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return "redirect:/auth/login";
        Long userId = userOpt.get().getUserId();
        
        Optional<Client> client = clientService.getClientByUserId(userId);
        if (userOpt.get().getRole() != UserRole.ADMIN) {
            if (client.isEmpty() || client.get().getRnokpp() == null || client.get().getPhone() == null || client.get().getFirstName() == null) {
                return "redirect:/profile/complete";
            }
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/auth/login";
        }
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return "redirect:/auth/login";
        Long userId = userOpt.get().getUserId();

        try {
            Optional<Client> client = clientService.getClientByUserId(userId);
            if (userOpt.get().getRole() != UserRole.ADMIN) {
                if (client.isEmpty() || client.get().getRnokpp() == null || client.get().getPhone() == null || client.get().getFirstName() == null) {
                    return "redirect:/profile/complete";
                }
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
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
     * Show order details
     */
    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "redirect:/auth/login";
        
        Optional<RentalOrder> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Замовлення не знайдено");
            return "redirect:/orders";
        }
        
        RentalOrder order = orderOpt.get();
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isPresent() && userOpt.get().getRole() != UserRole.ADMIN) {
            Optional<Client> client = clientService.getClientByUserId(userOpt.get().getUserId());
            if (client.isEmpty() || !client.get().getClientId().equals(order.getClientId())) {
                redirectAttributes.addFlashAttribute("error", "Доступ заборонено");
                return "redirect:/orders";
            }
        }
        
        model.addAttribute("order", order);
        model.addAttribute("ukLocale", new java.util.Locale("uk", "UA"));
        return "orders/details";
    }

    /**
     * Show rental contract
     */
    @GetMapping("/{id}/contract")
    public String orderContract(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return "redirect:/auth/login";

        Optional<RentalOrder> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty() || orderOpt.get().getStatus() == RentalOrder.OrderStatus.PENDING) {
            redirectAttributes.addFlashAttribute("error", "Договір доступний лише після підтвердження замовлення.");
            return "redirect:/orders";
        }

        RentalOrder order = orderOpt.get();
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        
        // Authorization check
        if (userOpt.isPresent() && userOpt.get().getRole() != UserRole.ADMIN) {
            Optional<Client> client = clientService.getClientByUserId(userOpt.get().getUserId());
            if (client.isEmpty() || !client.get().getClientId().equals(order.getClientId())) {
                redirectAttributes.addFlashAttribute("error", "Доступ заборонено");
                return "redirect:/orders";
            }
        }

        Optional<Client> clientOpt = clientService.getClientById(order.getClientId());
        
        model.addAttribute("order", order);
        model.addAttribute("client", clientOpt.orElse(new Client()));
        model.addAttribute("ukLocale", new java.util.Locale("uk", "UA"));
        model.addAttribute("title", "Договір оренди #" + order.getOrderId());
        
        return "orders/contract";
    }

    /**
     * Show pending orders (admin only)
     */
    @GetMapping("/pending")
    public String pendingOrders(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getAuthorities().stream().noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/";
        }

        List<RentalOrder> orders = orderService.getPendingOrders();
        model.addAttribute("orders", orders);
        return "orders/pending";
    }
}