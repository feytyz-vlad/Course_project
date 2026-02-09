package ua.com.kisit.course_project.Controller;

import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Service.RentalOrderService;

import java.time.LocalDate;
import java.util.List;

public class RentalOrderController {
    private final RentalOrderService orderService;

    public RentalOrderController(RentalOrderService orderService) {
        this.orderService = orderService;
    }

    public RentalOrder createOrder(Long clientId, Long carId, LocalDate startDate, LocalDate endDate) {
        try {
            return orderService.createOrder(clientId, carId, startDate, endDate);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error creating order: " + e.getMessage());
            return null;
        }
    }

    public boolean approveOrder(Long orderId) {
        try {
            return orderService.approveOrder(orderId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error approving order: " + e.getMessage());
            return false;
        }
    }

    public boolean rejectOrder(Long orderId, String reason) {
        try {
            return orderService.rejectOrder(orderId, reason);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error rejecting order: " + e.getMessage());
            return false;
        }
    }

    public boolean completeOrder(Long orderId, LocalDate actualReturnDate) {
        try {
            return orderService.completeOrder(orderId, actualReturnDate);
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Error completing order: " + e.getMessage());
            return false;
        }
    }

    public List<RentalOrder> getClientOrders(Long clientId) {
        return orderService.getClientOrders(clientId);
    }

    public List<RentalOrder> getPendingOrders() {
        return orderService.getPendingOrders();
    }

    public List<RentalOrder> getActiveOrders() {
        return orderService.getActiveOrders();
    }

    public List<RentalOrder> getAllOrders() {
        return orderService.getAllOrders();
    }

    public void displayOrderList(List<RentalOrder> orders) {
        System.out.println("\n=== Список замовлень ===");
        if (orders.isEmpty()) {
            System.out.println("Замовлень не знайдено");
            return;
        }

        for (RentalOrder order : orders) {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("ID замовлення: " + order.getOrderId());
            System.out.println("Дата початку: " + order.getStartDate());
            System.out.println("Дата закінчення: " + order.getEndDate());
            System.out.println("Загальна вартість: " + order.getTotalCost() + " грн");
            System.out.println("Статус: " + order.getStatus().getDisplayName());
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}