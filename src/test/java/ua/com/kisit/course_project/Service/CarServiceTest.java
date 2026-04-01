package ua.com.kisit.course_project.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Repository.CarRepository;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    @Test
    void shouldReturnOnlyAvailableCars() {
        Car availableCar = new Car("Toyota", "Camry", 2020, "AA1111BB",
                Car.TransmissionType.AUTOMATIC, Car.FuelType.PETROL, 5, new BigDecimal("1000"));
        availableCar.setStatus(Car.CarStatus.AVAILABLE);
        availableCar.setCarId(1L);

        Car rentedCar = new Car("Ford", "Focus", 2018, "BB2222CC",
                Car.TransmissionType.MANUAL, Car.FuelType.DIESEL, 5, new BigDecimal("700"));
        rentedCar.setStatus(Car.CarStatus.RENTED);
        rentedCar.setCarId(2L);

        when(carRepository.findAll()).thenReturn(List.of(availableCar, rentedCar));

        List<Car> result = carService.getAvailableCars();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Toyota Camry 2020", result.get(0).getFullName());
        verify(carRepository, times(1)).findAll();
    }

    @Test
    void shouldUpdateCarStatusWhenExists() {
        Car car = new Car("Honda", "Civic", 2019, "CC3333DD",
                Car.TransmissionType.MANUAL, Car.FuelType.PETROL, 5, new BigDecimal("800"));
        car.setCarId(10L);
        car.setStatus(Car.CarStatus.AVAILABLE);

        when(carRepository.findById(10L)).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = carService.updateCarStatus(10L, Car.CarStatus.RENTED);

        assertTrue(result);
        assertEquals(Car.CarStatus.RENTED, car.getStatus());

        ArgumentCaptor<Car> carCaptor = ArgumentCaptor.forClass(Car.class);
        verify(carRepository).save(carCaptor.capture());
        assertEquals(Car.CarStatus.RENTED, carCaptor.getValue().getStatus());
    }
}
