package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.Payment;
import ua.com.kisit.course_project.Entity.Payment.*;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Optional<Payment> findById(Long paymentId);
    Payment save(Payment payment);
    Payment update(Payment payment);
    boolean deleteById(Long paymentId);
    List<Payment> findAll();
    List<Payment> findByOrderId(Long orderId);
    List<Payment> findByStatus(PaymentStatus status);
    List<Payment> findByType(PaymentType type);
    boolean updateStatus(Long paymentId, PaymentStatus newStatus);
}