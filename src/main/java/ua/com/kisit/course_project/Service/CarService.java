package ua.com.kisit.course_project.Service;

import org.springframework.stereotype.Service;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Car.*;
import ua.com.kisit.course_project.Repository.CarRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service  // FIXED: додана анотація — без неї Spring не бачить цей клас як bean
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car addCar(Car car) {
        if (carRepository.existsByRegistrationNumber(car.getRegistrationNumber())) {
            throw new IllegalArgumentException("Автомобіль з таким номером вже існує");
        }
        return carRepository.save(car);
    }

    public Car updateCar(Car car) {
        Optional<Car> existing = carRepository.findById(car.getCarId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Автомобіль не знайдено");
        }
        return carRepository.update(car);
    }

    public boolean deleteCar(Long carId) {
        return carRepository.deleteById(carId);
    }

    public Optional<Car> getCarById(Long carId) {
        return carRepository.findById(carId);
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public List<Car> getAvailableCars() {
        return carRepository.findAvailableCars();
    }

    public List<Car> searchCars(String brand, CarStatus status, TransmissionType transmission,
                                FuelType fuel, BigDecimal maxPrice) {
        return carRepository.searchCars(brand, status, transmission, fuel, maxPrice);
    }

    public boolean updateCarStatus(Long carId, CarStatus newStatus) {
        return carRepository.updateStatus(carId, newStatus);
    }

    public boolean markAsAvailable(Long carId) {
        return updateCarStatus(carId, CarStatus.AVAILABLE);
    }

    public boolean markAsRented(Long carId) {
        return updateCarStatus(carId, CarStatus.RENTED);
    }

    public boolean markAsDamaged(Long carId) {
        return updateCarStatus(carId, CarStatus.DAMAGED);
    }

    public long getTotalCarsCount() {
        return carRepository.countAll();
    }

    public long getAvailableCarsCount() {
        return carRepository.countAvailable();
    }
}