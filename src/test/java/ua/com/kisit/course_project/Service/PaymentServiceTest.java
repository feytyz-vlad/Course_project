package ua.com.kisit.course_project.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
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

import ua.com.kisit.course_project.Entity.Payment;
import ua.com.kisit.course_project.Entity.Payment.PaymentMethod;
import ua.com.kisit.course_project.Entity.Payment.PaymentStatus;
import ua.com.kisit.course_project.Entity.Payment.PaymentType;
import ua.com.kisit.course_project.Entity.RentalOrder;
import ua.com.kisit.course_project.Repository.PaymentRepository;
import ua.com.kisit.course_project.Repository.RentalOrderRepository;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private RentalOrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void getPaymentsByOrder_Success() {
        List<Payment> payments = List.of(new Payment());
        when(paymentRepository.findByOrderId(10L)).thenReturn(payments);

        List<Payment> result = paymentService.getPaymentsByOrder(10L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void initiatePayment_Success() {
        RentalOrder order = new RentalOrder();
        order.setOrderId(10L);
        order.setTotalCost(new BigDecimal("2500"));

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(10L)).thenReturn(new ArrayList<>());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.initiatePayment(10L);

        assertNotNull(payment);
        assertEquals(10L, payment.getOrderId());
        assertEquals(new BigDecimal("2500"), payment.getAmount());
        assertEquals(PaymentType.RENTAL, payment.getPaymentType());
        assertEquals(PaymentMethod.ONLINE, payment.getPaymentMethod());
        assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void initiatePayment_ThrowsException_WhenOrderNotFound() {
        when(orderRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.initiatePayment(10L);
        });

        assertEquals("Замовлення не знайдено", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void initiatePayment_ThrowsException_WhenAlreadyPaid() {
        RentalOrder order = new RentalOrder();
        order.setOrderId(10L);

        Payment completedPayment = new Payment(10L, new BigDecimal("2500"), PaymentType.RENTAL, PaymentMethod.ONLINE);
        completedPayment.setPaymentStatus(PaymentStatus.COMPLETED);

        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(10L)).thenReturn(List.of(completedPayment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            paymentService.initiatePayment(10L);
        });

        assertEquals("Замовлення вже оплачено", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void initiateDamagePayment_Success() {
        when(paymentRepository.findByOrderId(10L)).thenReturn(new ArrayList<>());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Payment payment = paymentService.initiateDamagePayment(10L, 5L, new BigDecimal("500"));

        assertNotNull(payment);
        assertEquals(10L, payment.getOrderId());
        assertEquals(new BigDecimal("500"), payment.getAmount());
        assertEquals(PaymentType.DAMAGE, payment.getPaymentType());
        assertTrue(payment.getNotes().contains("Report #5"));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void initiateDamagePayment_ThrowsException_WhenDamageAlreadyPaid() {
        Payment paidDamage = new Payment(10L, new BigDecimal("500"), PaymentType.DAMAGE, PaymentMethod.ONLINE);
        paidDamage.setPaymentStatus(PaymentStatus.COMPLETED);
        paidDamage.setNotes("Damage Payment for Report #5");

        when(paymentRepository.findByOrderId(10L)).thenReturn(List.of(paidDamage));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            paymentService.initiateDamagePayment(10L, 5L, new BigDecimal("500"));
        });

        assertEquals("Це пошкодження вже оплачено", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void processWayForPayPayment_Success() {
        Payment payment = new Payment(10L, new BigDecimal("2000"), PaymentType.RENTAL, PaymentMethod.ONLINE);
        payment.setPaymentId(100L);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        RentalOrder order = new RentalOrder();
        order.setOrderId(10L);
        order.setStatus(RentalOrder.OrderStatus.WAITING_FOR_PAYMENT);

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));
        when(orderRepository.findById(10L)).thenReturn(Optional.of(order));

        boolean result = paymentService.processWayForPayPayment(100L, true, "11112222", "email@test.com", "+38050");

        assertTrue(result);
        assertEquals(PaymentStatus.COMPLETED, payment.getPaymentStatus());
        assertNotNull(payment.getTransactionId());
        verify(paymentRepository, times(1)).update(payment);
        verify(orderRepository, times(1)).updatePaymentInfo(10L, true, "11112222", "email@test.com", "+38050");
        verify(orderRepository, times(1)).updateStatus(10L, RentalOrder.OrderStatus.PENDING);
    }

    @Test
    void processWayForPayPayment_Failure() {
        Payment payment = new Payment(10L, new BigDecimal("2000"), PaymentType.RENTAL, PaymentMethod.ONLINE);
        payment.setPaymentId(100L);
        payment.setPaymentStatus(PaymentStatus.PENDING);

        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));

        boolean result = paymentService.processWayForPayPayment(100L, false, null, null, null);

        assertFalse(result);
        assertEquals(PaymentStatus.FAILED, payment.getPaymentStatus());
        verify(paymentRepository, times(1)).update(payment);
    }

    @Test
    void getPaymentById_Success() {
        Payment payment = new Payment();
        payment.setPaymentId(100L);
        when(paymentRepository.findById(100L)).thenReturn(Optional.of(payment));

        Optional<Payment> result = paymentService.getPaymentById(100L);

        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getPaymentId());
    }
}
