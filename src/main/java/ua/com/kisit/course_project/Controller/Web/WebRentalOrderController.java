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

    private static final String REDIRECT_AUTH_LOGIN = "redirect:/auth/login";
    private static final String REDIRECT_ORDERS = "redirect:/orders";
    private static final String REDIRECT_PROFILE_COMPLETE = "redirect:/profile/complete";

    private final RentalOrderService orderService;
    private final CarService carService;
    private final ClientService clientService;
    private final UserRepository userRepository;
    private final ua.com.kisit.course_project.Service.DamageReportService damageService;
    private final ua.com.kisit.course_project.Service.PaymentService paymentService;

    public WebRentalOrderController(RentalOrderService orderService,
                                    CarService carService,
                                    ClientService clientService,
                                    UserRepository userRepository,
                                    ua.com.kisit.course_project.Service.DamageReportService damageService,
                                    ua.com.kisit.course_project.Service.PaymentService paymentService) {
        this.orderService = orderService;
        this.carService = carService;
        this.clientService = clientService;
        this.userRepository = userRepository;
        this.damageService = damageService;
        this.paymentService = paymentService;
    }

    /**
     * Show all orders (for admin)
     */
    @GetMapping
    public String listOrders(HttpSession session, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || WebAuthController.ANONYMOUS_USER.equals(auth.getPrincipal())) {
            return REDIRECT_AUTH_LOGIN;
        }
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return REDIRECT_AUTH_LOGIN;
        
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
                return REDIRECT_PROFILE_COMPLETE;
            }
        }
    }

    /**
     * Show create order form
     */
    @GetMapping("/create")
    public String showCreateForm(@RequestParam Long carId, Model model, HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || WebAuthController.ANONYMOUS_USER.equals(auth.getPrincipal())) {
            return REDIRECT_AUTH_LOGIN;
        }
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return REDIRECT_AUTH_LOGIN;
        Long userId = userOpt.get().getUserId();
        
        Optional<Client> client = clientService.getClientByUserId(userId);
        if (userOpt.get().getRole() != UserRole.ADMIN) {
            if (client.isEmpty() || client.get().getRnokpp() == null || client.get().getPhone() == null || client.get().getFirstName() == null) {
                return REDIRECT_PROFILE_COMPLETE;
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
        if (auth == null || !auth.isAuthenticated() || WebAuthController.ANONYMOUS_USER.equals(auth.getPrincipal())) {
            return REDIRECT_AUTH_LOGIN;
        }
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) return REDIRECT_AUTH_LOGIN;
        Long userId = userOpt.get().getUserId();

        try {
            Optional<Client> client = clientService.getClientByUserId(userId);
            if (userOpt.get().getRole() != UserRole.ADMIN) {
                if (client.isEmpty() || client.get().getRnokpp() == null || client.get().getPhone() == null || client.get().getFirstName() == null) {
                    return REDIRECT_PROFILE_COMPLETE;
                }
            }

            RentalOrder order = orderService.createOrder(
                    client.get().getClientId(),
                    carId,
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate)
            );

            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_SUCCESS, "Замовлення створено! Будь ласка, оплатіть оренду.");
            return "redirect:/payment/checkout/" + order.getOrderId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, e.getMessage());
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
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_SUCCESS, "Замовлення затверджено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, e.getMessage());
        }

        return REDIRECT_ORDERS;
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
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_SUCCESS, "Замовлення відхилено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, e.getMessage());
        }

        return REDIRECT_ORDERS;
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
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_SUCCESS, "Замовлення завершено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, e.getMessage());
        }

        return REDIRECT_ORDERS;
    }

    /**
     * Show order details
     */
    @GetMapping("/{id}")
    public String orderDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return REDIRECT_AUTH_LOGIN;
        
        Optional<RentalOrder> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, "Замовлення не знайдено");
            return REDIRECT_ORDERS;
        }
        
        RentalOrder order = orderOpt.get();
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isPresent() && userOpt.get().getRole() != UserRole.ADMIN) {
            Optional<Client> client = clientService.getClientByUserId(userOpt.get().getUserId());
            if (client.isEmpty() || !client.get().getClientId().equals(order.getClientId())) {
                redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, "Доступ заборонено");
                return REDIRECT_ORDERS;
            }
        }
        
        model.addAttribute("order", order);
        model.addAttribute("damageReports", damageService.getReportsByOrderId(id));
        model.addAttribute("payments", paymentService.getPaymentsByOrder(id));
        model.addAttribute("ukLocale", new java.util.Locale("uk", "UA"));
        return "orders/details";
    }

    /**
     * Report damage (Admin/Manager only)
     */
    @PostMapping("/{id}/report-damage")
    public String reportDamage(@PathVariable Long id,
                               @RequestParam String description,
                               @RequestParam java.math.BigDecimal repairCost,
                               @RequestParam java.math.BigDecimal fineAmount,
                               RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return REDIRECT_AUTH_LOGIN;
        
        Optional<User> adminOpt = userRepository.findByEmail(auth.getName());
        if (adminOpt.isEmpty() || (adminOpt.get().getRole() != UserRole.ADMIN && adminOpt.get().getRole() != UserRole.MANAGER)) {
            return "redirect:/";
        }

        try {
            Optional<RentalOrder> order = orderService.getOrderById(id);
            if (order.isPresent()) {
                damageService.createReport(
                        id, 
                        order.get().getCarId(), 
                        description, 
                        LocalDate.now(), 
                        repairCost, 
                        fineAmount, 
                        adminOpt.get().getUserId()
                );
                redirectAttributes.addFlashAttribute(WebAuthController.ATTR_SUCCESS, "Звіт про пошкодження додано! Статус авто змінено.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, "Помилка: " + e.getMessage());
        }
        return "redirect:/orders/" + id;
    }

    /**
     * Pay for damage (Client only)
     */
    @PostMapping("/{id}/pay-damage/{reportId}")
    public String payDamage(@PathVariable Long id,
                            @PathVariable Long reportId,
                            RedirectAttributes redirectAttributes) {
        return "redirect:/payment/checkout/damage/" + reportId;
    }

    /**
     * Show rental contract
     */
    @GetMapping("/{id}/contract")
    public String orderContract(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return REDIRECT_AUTH_LOGIN;

        Optional<RentalOrder> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty() || orderOpt.get().getStatus() == RentalOrder.OrderStatus.PENDING) {
            redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, "Договір доступний лише після підтвердження замовлення.");
            return REDIRECT_ORDERS;
        }

        RentalOrder order = orderOpt.get();
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        
        // Authorization check
        if (userOpt.isPresent() && userOpt.get().getRole() != UserRole.ADMIN) {
            Optional<Client> client = clientService.getClientByUserId(userOpt.get().getUserId());
            if (client.isEmpty() || !client.get().getClientId().equals(order.getClientId())) {
                redirectAttributes.addFlashAttribute(WebAuthController.ATTR_ERROR, "Доступ заборонено");
                return REDIRECT_ORDERS;
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