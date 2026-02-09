package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Entity.RentalOrder.OrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentalOrderRepository {

    Optional<RentalOrder> findById(Long orderId);
    RentalOrder save(RentalOrder order);
    RentalOrder update(RentalOrder order);
    boolean deleteById(Long orderId);
    List<RentalOrder> findAll();
    List<RentalOrder> findByClientId(Long clientId);
    List<RentalOrder> findByCarId(Long carId);
    List<RentalOrder> findByStatus(OrderStatus status);
    List<RentalOrder> findActiveOrders();
    List<RentalOrder> findPendingOrders();
    List<RentalOrder> findByDateRange(LocalDate startDate, LocalDate endDate);
    boolean updateStatus(Long orderId, OrderStatus newStatus);
    boolean updateActualReturnDate(Long orderId, LocalDate returnDate);
    boolean reject(Long orderId, String rejectionReason);
    List<RentalOrder> findOverdueOrders();
    long countByStatus(OrderStatus status);
}