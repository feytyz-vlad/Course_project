package ua.com.kisit.course_project.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import ua.com.kisit.course_project.Annotation.Auditable;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Entity.RentalOrder.OrderStatus;
import ua.com.kisit.course_project.Repository.CarRepository;
import ua.com.kisit.course_project.Repository.RentalOrderRepository;

@Service
public class RentalOrderService {
    private final RentalOrderRepository orderRepository;
    private final CarRepository carRepository;

    public RentalOrderService(RentalOrderRepository orderRepository, CarRepository carRepository) {
        this.orderRepository = orderRepository;
        this.carRepository = carRepository;
    }

    @Auditable(action = "CREATE_ORDER")
    public RentalOrder createOrder(Long clientId, Long carId, LocalDate startDate, LocalDate endDate) {
        Optional<Car> carOpt = carRepository.findById(carId);
        if (carOpt.isEmpty()) {
            throw new IllegalArgumentException("Автомобіль не знайдено");
        }

        Car car = carOpt.get();
        if (!car.isAvailable()) {
            throw new IllegalStateException("Автомобіль недоступний для оренди");
        }

        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата початку не може бути пізніше дати закінчення");
        }

        int totalDays = (int) ChronoUnit.DAYS.between(startDate, endDate);
        if (totalDays < 1) {
            totalDays = 1;
        }

        BigDecimal totalCost = car.getDailyRate().multiply(BigDecimal.valueOf(totalDays));

        RentalOrder order = new RentalOrder(clientId, carId, startDate, endDate,
                totalDays, car.getDailyRate(), totalCost);
        return orderRepository.save(order);
    }

    @Auditable(action = "APPROVE_ORDER")
    public boolean approveOrder(Long orderId) {
        Optional<RentalOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Замовлення не знайдено");
        }

        RentalOrder order = orderOpt.get();
        if (!order.canBeApproved()) {
            throw new IllegalStateException("Замовлення не може бути затверджено");
        }

        // Перевіряємо, чи авто все ще доступне
        Optional<Car> carOpt = carRepository.findById(order.getCarId());
        if (carOpt.isEmpty() || !carOpt.get().isAvailable()) {
            throw new IllegalStateException("Автомобіль вже орендований або недоступний для оренди");
        }

        // 1. Оновлюємо статус замовлення на ACTIVE
        boolean updated = orderRepository.updateStatus(orderId, OrderStatus.ACTIVE);
        
        // 2. Якщо статус замовлення оновлено, автоматично змінюємо статус авто на RENTED
        if (updated) {
            carRepository.updateStatus(order.getCarId(), Car.CarStatus.RENTED);
        }
        return updated;
    }

    @Auditable(action = "REJECT_ORDER")
    public boolean rejectOrder(Long orderId, String reason) {
        Optional<RentalOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Замовлення не знайдено");
        }

        RentalOrder order = orderOpt.get();
        if (!order.canBeRejected()) {
            throw new IllegalStateException("Замовлення не може бути відхилено");
        }

        return orderRepository.reject(orderId, reason);
    }

    @Auditable(action = "COMPLETE_ORDER")
    public boolean completeOrder(Long orderId, LocalDate actualReturnDate) {
        Optional<RentalOrder> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            throw new IllegalArgumentException("Замовлення не знайдено");
        }

        RentalOrder order = orderOpt.get();
        if (!order.isActive()) {
            throw new IllegalStateException("Тільки активні замовлення можуть бути завершені");
        }

        orderRepository.updateActualReturnDate(orderId, actualReturnDate);
        
        // 1. Оновлюємо статус замовлення на COMPLETED
        boolean updated = orderRepository.updateStatus(orderId, OrderStatus.COMPLETED);

        // 2. Якщо замовлення завершено, повертаємо авто у статус AVAILABLE
        if (updated) {
            carRepository.updateStatus(order.getCarId(), Car.CarStatus.AVAILABLE);
        }
        return updated;
    }

    public List<RentalOrder> getClientOrders(Long clientId) {
        List<RentalOrder> orders = orderRepository.findByClientId(clientId);
        populateCarDetails(orders);
        return orders;
    }

    public List<RentalOrder> getPendingOrders() {
        List<RentalOrder> orders = orderRepository.findPendingOrders();
        populateCarDetails(orders);
        return orders;
    }

    public List<RentalOrder> getActiveOrders() {
        List<RentalOrder> orders = orderRepository.findActiveOrders();
        populateCarDetails(orders);
        return orders;
    }

    public List<RentalOrder> getAllOrders() {
        List<RentalOrder> orders = orderRepository.findAll();
        populateCarDetails(orders);
        return orders;
    }

    public List<RentalOrder> getClientActiveOrders(Long clientId) {
        List<RentalOrder> orders = orderRepository.findByClientIdAndStatus(clientId, OrderStatus.ACTIVE);
        populateCarDetails(orders);
        return orders;
    }

    public Optional<RentalOrder> getOrderById(Long id) {
        Optional<RentalOrder> orderOpt = orderRepository.findById(id);
        orderOpt.ifPresent(order -> carRepository.findById(order.getCarId()).ifPresent(order::setCar));
        return orderOpt;
    }

    public List<RentalOrder> getLatestOrders(int limit) {
        List<RentalOrder> orders = orderRepository.findLatestOrders(limit);
        populateCarDetails(orders);
        return orders;
    }

    private void populateCarDetails(List<RentalOrder> orders) {
        for (RentalOrder order : orders) {
            carRepository.findById(order.getCarId()).ifPresent(order::setCar);
        }
    }
}