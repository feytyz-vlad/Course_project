package ua.com.kisit.course_project.Controller.Web;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.com.kisit.course_project.Entity.Payment;
import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Service.PaymentService;
import ua.com.kisit.course_project.Service.RentalOrderService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final RentalOrderService orderService;
    private final ua.com.kisit.course_project.Service.WayForPayService wfpService;
    private final ua.com.kisit.course_project.Service.DamageReportService damageService;

    public PaymentController(PaymentService paymentService, 
                             RentalOrderService orderService, 
                             ua.com.kisit.course_project.Service.WayForPayService wfpService,
                             ua.com.kisit.course_project.Service.DamageReportService damageService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.wfpService = wfpService;
        this.damageService = damageService;
    }

    @GetMapping("/checkout/{orderId}")
    public String checkout(@PathVariable Long orderId, Model model, RedirectAttributes redirectAttributes) {
        try {
            RentalOrder order = orderService.getOrderById(orderId).orElseThrow();
            Payment payment = paymentService.initiatePayment(orderId);
            
            long orderDate = System.currentTimeMillis() / 1000;
            String orderRef = "ORDER-" + orderId + "-" + payment.getPaymentId();
            
            List<String> productNames = List.of("Car Rental Order " + order.getCarId());
            List<Integer> productCounts = List.of(1);
            String amountStr = String.format(java.util.Locale.US, "%.2f", order.getTotalCost());
            String priceStr = amountStr;
            String countStr = "1";

            String signature = wfpService.generateSignature(
                orderRef, 
                orderDate, 
                amountStr, 
                "UAH", 
                productNames, 
                List.of(countStr), 
                List.of(priceStr)
            );

            model.addAttribute("merchantAccount", wfpService.getMerchantAccount());
            model.addAttribute("merchantDomainName", wfpService.getMerchantDomainName());
            model.addAttribute("orderReference", orderRef);
            model.addAttribute("orderDate", String.valueOf(orderDate));
            model.addAttribute("amount", amountStr);
            model.addAttribute("currency", "UAH");
            model.addAttribute("productName", productNames.get(0));
            model.addAttribute("productPrice", priceStr);
            model.addAttribute("productCount", countStr);
            model.addAttribute("merchantSignature", signature);
            model.addAttribute("returnUrl", "http://localhost:8080/payment/return/" + payment.getPaymentId());
            model.addAttribute("serviceUrl", "http://localhost:8080/payment/callback");
            
            return "payment/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders/" + orderId;
        }
    }

    @GetMapping("/checkout/damage/{reportId}")
    public String checkoutDamage(@PathVariable Long reportId, Model model, RedirectAttributes redirectAttributes) {
        try {
            ua.com.kisit.course_project.Entity.DamageReport report = damageService.getReportById(reportId).orElseThrow();
            Payment payment = paymentService.initiateDamagePayment(report.getOrderId(), reportId, report.getTotalAmount());
            
            long orderDate = System.currentTimeMillis() / 1000;
            String orderRef = "DAMAGE-" + reportId + "-" + payment.getPaymentId();
            
            List<String> productNames = List.of("Damage Payment " + reportId);
            List<Integer> productCounts = List.of(1);
            String amountStr = String.format(java.util.Locale.US, "%.2f", report.getTotalAmount());
            String priceStr = amountStr;
            String countStr = "1";

            String signature = wfpService.generateSignature(
                orderRef, 
                orderDate, 
                amountStr, 
                "UAH", 
                productNames, 
                List.of(countStr), 
                List.of(priceStr)
            );

            model.addAttribute("merchantAccount", wfpService.getMerchantAccount());
            model.addAttribute("merchantDomainName", wfpService.getMerchantDomainName());
            model.addAttribute("orderReference", orderRef);
            model.addAttribute("orderDate", String.valueOf(orderDate));
            model.addAttribute("amount", amountStr);
            model.addAttribute("currency", "UAH");
            model.addAttribute("productName", productNames.get(0));
            model.addAttribute("productPrice", priceStr);
            model.addAttribute("productCount", countStr);
            model.addAttribute("merchantSignature", signature);
            model.addAttribute("returnUrl", "http://localhost:8080/payment/return/" + payment.getPaymentId());
            model.addAttribute("serviceUrl", "http://localhost:8080/payment/callback");
            
            return "payment/checkout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/orders";
        }
    }

    @RequestMapping(value = "/return/{paymentId}", method = {RequestMethod.GET, RequestMethod.POST})
    public String paymentReturn(@PathVariable Long paymentId, 
                                @RequestParam(required = false) String cardPan,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String phone,
                                HttpServletRequest request) {
        // WayForPay відправляє POST-redirect, через що браузер не надсилає session cookie
        // (SameSite=Lax policy). Тому тут ми обробляємо платіж і редиректимо
        // на GET-сторінку успіху в межах /payment/** (яка permitAll)
        paymentService.processWayForPayPayment(paymentId, true, cardPan, email, phone);
        
        Optional<Payment> p = paymentService.getPaymentById(paymentId);
        if (p.isPresent() && p.get().getPaymentType() == Payment.PaymentType.DAMAGE) {
            // Extract reportId from notes "Damage Payment for Report #123"
            String notes = p.get().getNotes();
            if (notes != null && notes.contains("#")) {
                try {
                    Long reportId = Long.parseLong(notes.substring(notes.indexOf("#") + 1));
                    damageService.payDamage(reportId);
                } catch (Exception e) {
                    System.err.println("Failed to parse reportId from payment notes: " + e.getMessage());
                }
            }
        }
        
        Long orderId = p.map(Payment::getOrderId).orElse(0L);
        // Редирект на GET-сторінку успіху (permitAll), а не на /orders/ (потрібна аутентифікація)
        return "redirect:/payment/success?orderId=" + orderId;
    }

    /**
     * Сторінка успішної оплати — permitAll, показує результат та посилання на замовлення.
     * Це дозволяє відновити сесію через GET-навігацію після cross-site POST від WayForPay.
     */
    @GetMapping("/success")
    public String paymentSuccess(@RequestParam(defaultValue = "0") Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        model.addAttribute("successMessage", "Платіж успішно отримано! Очікуйте на підтвердження адміністратором.");
        return "payment/success";
    }

    @GetMapping("/mock-wayforpay/{paymentId}")
    public String showMockWayForPay(@PathVariable Long paymentId, Model model) {
        Optional<Payment> paymentOpt = paymentService.getPaymentById(paymentId);
        if (paymentOpt.isEmpty()) {
            return "redirect:/orders";
        }

        Payment payment = paymentOpt.get();
        Optional<RentalOrder> orderOpt = orderService.getOrderById(payment.getOrderId());
        
        model.addAttribute("payment", payment);
        model.addAttribute("order", orderOpt.orElse(null));
        return "payment/wayforpay";
    }

    @PostMapping("/mock-process")
    public String processPayment(@RequestParam Long paymentId, 
                                 @RequestParam boolean success,
                                 @RequestParam(required = false) String cardPan,
                                 @RequestParam(required = false) String email,
                                 @RequestParam(required = false) String phone,
                                 RedirectAttributes redirectAttributes) {
        boolean result = paymentService.processWayForPayPayment(paymentId, success, cardPan, email, phone);
        
        Optional<Payment> paymentOpt = paymentService.getPaymentById(paymentId);
        Long orderId = paymentOpt.map(Payment::getOrderId).orElse(0L);

        if (result) {
            redirectAttributes.addFlashAttribute("success", "Оплата пройшла успішно!");
            return "redirect:/orders/" + orderId;
        } else {
            redirectAttributes.addFlashAttribute("error", "Помилка при проведенні оплати.");
            return "redirect:/orders/" + orderId;
        }
    }
}
