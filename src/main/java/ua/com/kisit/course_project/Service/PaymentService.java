package ua.com.kisit.course_project.Service;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import ua.com.kisit.course_project.Annotation.Auditable;
import ua.com.kisit.course_project.Entity.Payment;
import ua.com.kisit.course_project.Entity.Payment.PaymentMethod;
import ua.com.kisit.course_project.Entity.Payment.PaymentStatus;
import ua.com.kisit.course_project.Entity.Payment.PaymentType;
import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Repository.PaymentRepository;
import ua.com.kisit.course_project.Repository.RentalOrderRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final RentalOrderRepository orderRepository;

    public PaymentService(PaymentRepository paymentRepository, RentalOrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    public List<Payment> getPaymentsByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Auditable(action = "INIT_PAYMENT")
    public Payment initiatePayment(Long orderId) {
        Optional<RentalOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Замовлення не знайдено");
        }
        
        RentalOrder order = orderOpt.get();
        
        // Check if payment already exists
        List<Payment> existing = paymentRepository.findByOrderId(orderId);
        for (Payment p : existing) {
            if (p.isCompleted()) {
                throw new IllegalStateException("Замовлення вже оплачено");
            }
        }
        
        Payment payment = new Payment(orderId, order.getTotalCost(), PaymentType.RENTAL, PaymentMethod.ONLINE);
        return paymentRepository.save(payment);
    }

    @Auditable(action = "INIT_PAYMENT_DAMAGE")
    public Payment initiateDamagePayment(Long orderId, Long reportId, BigDecimal amount) {
        // Check if payment already exists for this report
        List<Payment> existing = paymentRepository.findByOrderId(orderId);
        for (Payment p : existing) {
            if (p.getPaymentType() == PaymentType.DAMAGE && p.isCompleted() && p.getNotes() != null && p.getNotes().contains("Report #" + reportId)) {
                throw new IllegalStateException("Це пошкодження вже оплачено");
            }
        }
        
        Payment payment = new Payment(orderId, amount, PaymentType.DAMAGE, PaymentMethod.ONLINE);
        payment.setNotes("Damage Payment for Report #" + reportId);
        return paymentRepository.save(payment);
    }

    @Auditable(action = "PROCESS_PAYMENT")
    public boolean processWayForPayPayment(Long paymentId, boolean success, String card, String email, String phone) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isEmpty()) {
            return false;
        }

        Payment payment = paymentOpt.get();
        if (success) {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            payment.setTransactionId("WFP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            paymentRepository.update(payment);
            
            // Update order info
            Optional<RentalOrder> orderOpt = orderRepository.findById(payment.getOrderId());
            if (orderOpt.isPresent()) {
                RentalOrder order = orderOpt.get();
                orderRepository.updatePaymentInfo(order.getOrderId(), true, card, email, phone);
                
                // If it was a rental payment, move it from WAITING_FOR_PAYMENT to PENDING (for admin review)
                if (payment.getPaymentType() == PaymentType.RENTAL && order.getStatus() == RentalOrder.OrderStatus.WAITING_FOR_PAYMENT) {
                    orderRepository.updateStatus(order.getOrderId(), RentalOrder.OrderStatus.PENDING);
                }
            }
            
            return true;
        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);
            paymentRepository.update(payment);
            return false;
        }
    }
    
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }
}
