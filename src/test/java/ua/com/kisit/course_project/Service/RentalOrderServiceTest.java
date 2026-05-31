package ua.com.kisit.course_project.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Entity.RentalOrder.OrderStatus;
import ua.com.kisit.course_project.Repository.CarRepository;
import ua.com.kisit.course_project.Repository.RentalOrderRepository;

@ExtendWith(MockitoExtension.class)
class RentalOrderServiceTest {

    @Mock
    private RentalOrderRepository orderRepository;

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private RentalOrderService rentalOrderService;

    @Test
    void createOrder_Success() {
        Car car = new Car("Toyota", "Camry", 2020, "AA1111BB",
                Car.TransmissionType.AUTOMATIC, Car.FuelType.PETROL, 5, new BigDecimal("1000"));
        car.setStatus(Car.CarStatus.AVAILABLE);
        car.setCarId(1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(orderRepository.save(any(RentalOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(5);

        RentalOrder order = rentalOrderService.createOrder(2L, 1L, start, end);

        assertNotNull(order);
        assertEquals(2L, order.getClientId());
        assertEquals(1L, order.getCarId());
        assertEquals(4, order.getTotalDays());
        assertEquals(new BigDecimal("4000"), order.getTotalCost());
        assertEquals(OrderStatus.WAITING_FOR_PAYMENT, order.getStatus());
        verify(orderRepository, times(1)).save(any(RentalOrder.class));
    }

    @Test
    void createOrder_ThrowsException_WhenCarNotFound() {
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(5);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            rentalOrderService.createOrder(2L, 1L, start, end);
        });

        assertEquals("Автомобіль не знайдено", exception.getMessage());
        verify(orderRepository, never()).save(any(RentalOrder.class));
    }

    @Test
    void createOrder_ThrowsException_WhenCarNotAvailable() {
        Car car = new Car();
        car.setStatus(Car.CarStatus.RENTED);
        car.setCarId(1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        LocalDate start = LocalDate.now().plusDays(1);
        LocalDate end = LocalDate.now().plusDays(5);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            rentalOrderService.createOrder(2L, 1L, start, end);
        });

        assertEquals("Автомобіль недоступний для оренди", exception.getMessage());
        verify(orderRepository, never()).save(any(RentalOrder.class));
    }

    @Test
    void createOrder_ThrowsException_WhenStartDateAfterEndDate() {
        Car car = new Car();
        car.setStatus(Car.CarStatus.AVAILABLE);
        car.setCarId(1L);

        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(1);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            rentalOrderService.createOrder(2L, 1L, start, end);
        });

        assertEquals("Дата початку не може бути пізніше дати закінчення", exception.getMessage());
        verify(orderRepository, never()).save(any(RentalOrder.class));
    }

    @Test
    void approveOrder_Success() {
        RentalOrder order = new RentalOrder(2L, 1L, LocalDate.now(), LocalDate.now().plusDays(2), 2, new BigDecimal("1000"), new BigDecimal("2000"));
        order.setOrderId(10L);
        order.setStatus(OrderStatus.PENDING); // waiting for manager approval

        Car car = new Car();
        car.setStatus(Car.CarStatus.AVAILABLE);
        car.setCarId(1L);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(orderRepository.updateStatus(10L, OrderStatus.ACTIVE)).thenReturn(true);

        boolean result = rentalOrderService.approveOrder(10L);

        assertTrue(result);
        verify(orderRepository, times(1)).updateStatus(10L, OrderStatus.ACTIVE);
        verify(carRepository, times(1)).updateStatus(1L, Car.CarStatus.RENTED);
    }

    @Test
    void approveOrder_ThrowsException_WhenOrderNotFound() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            rentalOrderService.approveOrder(10L);
        });

        assertEquals("Замовлення не знайдено", exception.getMessage());
    }

    @Test
    void approveOrder_ThrowsException_WhenCarNotAvailable() {
        RentalOrder order = new RentalOrder(2L, 1L, LocalDate.now(), LocalDate.now().plusDays(2), 2, new BigDecimal("1000"), new BigDecimal("2000"));
        order.setOrderId(10L);
        order.setStatus(OrderStatus.PENDING);

        Car car = new Car();
        car.setStatus(Car.CarStatus.RENTED);
        car.setCarId(1L);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            rentalOrderService.approveOrder(10L);
        });

        assertEquals("Автомобіль вже орендований або недоступний для оренди", exception.getMessage());
    }

    @Test
    void rejectOrder_Success() {
        RentalOrder order = new RentalOrder();
        order.setOrderId(10L);
        order.setStatus(OrderStatus.PENDING);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.reject(10L, "Driver license is not valid")).thenReturn(true);

        boolean result = rentalOrderService.rejectOrder(10L, "Driver license is not valid");

        assertTrue(result);
        verify(orderRepository, times(1)).reject(10L, "Driver license is not valid");
    }

    @Test
    void completeOrder_Success() {
        RentalOrder order = new RentalOrder();
        order.setOrderId(10L);
        order.setCarId(1L);
        order.setStatus(OrderStatus.ACTIVE);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(orderRepository.updateStatus(10L, OrderStatus.COMPLETED)).thenReturn(true);

        boolean result = rentalOrderService.completeOrder(10L, LocalDate.now());

        assertTrue(result);
        verify(orderRepository, times(1)).updateActualReturnDate(10L, LocalDate.now());
        verify(orderRepository, times(1)).updateStatus(10L, OrderStatus.COMPLETED);
        verify(carRepository, times(1)).updateStatus(1L, Car.CarStatus.AVAILABLE);
    }

    @Test
    void getClientOrders_Success() {
        RentalOrder order = new RentalOrder();
        order.setCarId(1L);
        List<RentalOrder> orders = List.of(order);

        Car car = new Car();
        car.setCarId(1L);

        when(orderRepository.findByClientId(5L)).thenReturn(orders);
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));

        List<RentalOrder> result = rentalOrderService.getClientOrders(5L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(car, result.get(0).getCar());
    }
}
