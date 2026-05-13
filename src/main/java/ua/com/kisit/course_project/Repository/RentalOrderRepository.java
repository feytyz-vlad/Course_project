package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Entity.RentalOrder.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalOrderRepository {

    RentalOrder save(RentalOrder order);

    Optional<RentalOrder> findById(Long orderId);

    List<RentalOrder> findAll();

    List<RentalOrder> findByClientId(Long clientId);

    List<RentalOrder> findByClientIdAndStatus(Long clientId, OrderStatus status);

    List<RentalOrder> findPendingOrders();

    List<RentalOrder> findActiveOrders();

    boolean updateStatus(Long orderId, OrderStatus status);

    boolean reject(Long orderId, String reason);

    boolean updateActualReturnDate(Long orderId, LocalDate actualReturnDate);

    boolean deleteById(Long orderId);

    List<RentalOrder> findLatestOrders(int limit);

    boolean updatePaymentInfo(Long orderId, boolean isPaid, String card, String email, String phone);
}