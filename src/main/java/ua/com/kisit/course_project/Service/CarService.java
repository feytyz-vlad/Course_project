package ua.com.kisit.course_project.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Car.CarStatus;
import ua.com.kisit.course_project.Entity.Car.FuelType;
import ua.com.kisit.course_project.Entity.Car.TransmissionType;
import ua.com.kisit.course_project.Repository.CarRepository;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    public Car addCar(Car car) {
        if (car.getRegistrationNumber() != null) {
            boolean exists = carRepository.findAll()
                    .stream()
                    .anyMatch(c -> car.getRegistrationNumber().equalsIgnoreCase(c.getRegistrationNumber()));
            if (exists) {
                throw new IllegalArgumentException("Автомобіль з таким номером вже існує");
            }
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
        Optional<Car> car = carRepository.findById(carId);
        if (car.isEmpty()) return false;
        carRepository.deleteById(carId);
        return true;
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

    public List<Car> getAllCarsPaginated(int page, int size) {
        int offset = page * size;
        return carRepository.findAllPaginated(size, offset);
    }

    public List<Car> getAvailableCarsPaginated(int page, int size) {
        int offset = page * size;
        return carRepository.findAvailablePaginated(size, offset);
    }

    public List<Car> searchCars(String brand, CarStatus status, TransmissionType transmission,
                                FuelType fuel, BigDecimal maxPrice) {
        return carRepository.findAll()
                .stream()
                .filter(c -> brand == null || brand.isBlank() ||
                        (c.getBrand() != null && c.getBrand().toLowerCase().contains(brand.toLowerCase())))
                .filter(c -> status == null || c.getStatus() == status)
                .filter(c -> transmission == null || c.getTransmissionType() == transmission)
                .filter(c -> fuel == null || c.getFuelType() == fuel)
                .filter(c -> maxPrice == null || c.getDailyRate() == null || c.getDailyRate().compareTo(maxPrice) <= 0)
                .collect(Collectors.toList());
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