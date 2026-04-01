package ua.com.kisit.course_project.Entity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class CarTest {

    @Test
    void testGetFullName() {
        Car car = new Car("Toyota", "Camry", 2020, "AA1111BB",
                Car.TransmissionType.AUTOMATIC, Car.FuelType.PETROL, 5,
                new BigDecimal("1000"));
        assertEquals("Toyota Camry 2020", car.getFullName());
    }

    @Test
    void testAvailabilityToggle() {
        Car car = new Car("Honda", "Civic", 2019, "BB2222CC",
                Car.TransmissionType.MANUAL, Car.FuelType.PETROL, 5,
                new BigDecimal("800"));
        // по умолчанию в конструкторе статус AVAILABLE
        assertTrue(car.isAvailable(), "Ожидается, что новая машина по умолчанию доступна");

        car.setStatus(Car.CarStatus.RENTED);
        assertFalse(car.isAvailable(), "Ожидается, что машина со статусом RENTED недоступна");
    }
}
