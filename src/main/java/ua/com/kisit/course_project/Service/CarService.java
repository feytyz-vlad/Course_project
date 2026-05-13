package ua.com.kisit.course_project.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import ua.com.kisit.course_project.Annotation.Auditable;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Car.CarStatus;
import ua.com.kisit.course_project.Entity.Car.FuelType;
import ua.com.kisit.course_project.Repository.CarRepository;

@Service
public class CarService {

    private final CarRepository carRepository;

    public CarService(CarRepository carRepository) {
        this.carRepository = carRepository;
    }

    @Auditable(action = "ADD_CAR")
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

    @Auditable(action = "UPDATE_CAR")
    public Car updateCar(Car car) {
        Optional<Car> existing = carRepository.findById(car.getCarId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Автомобіль не знайдено");
        }
        return carRepository.update(car);
    }

    @Auditable(action = "DELETE_CAR")
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
        // Попытка напрямую через репозиторий
        try {
            List<Car> available = carRepository.findAvailableCars();
            if (available != null && !available.isEmpty()) {
                return available;
            }
        } catch (Exception ignored) {
            // если репозиторий не реализует метод корректно — будем фоллбэкить
        }

        // Фоллбэк: берём все машины и фильтруем по статусу
        List<Car> all = carRepository.findAll();
        return all.stream()
                  .filter(Car::isAvailable)
                  .collect(Collectors.toList());
    }

    public List<Car> getAllCarsPaginated(int page, int size) {
        int offset = page * size;
        return carRepository.findAllPaginated(size, offset);
    }

    public List<Car> getAvailableCarsPaginated(int page, int size) {
        int offset = page * size;
        return carRepository.findAvailablePaginated(size, offset);
    }

    public List<Car> searchCars(String query, Car.CarClass carClass, FuelType fuel, Integer seats, Integer year, 
                                BigDecimal minPrice, BigDecimal maxPrice, String sortOrder,
                                Double userLat, Double userLng, Double maxDistance) {
        return carRepository.searchCars(query, carClass, fuel, seats, year, minPrice, maxPrice, sortOrder, userLat, userLng, maxDistance);
    }

    public boolean updateCarStatus(Long carId, CarStatus newStatus) {
        // Сначала пробуем обновить напрямую
        try {
            boolean ok = carRepository.updateStatus(carId, newStatus);
            if (ok) return true;
        } catch (Exception ignored) {
            // продолжим с фоллбеком
        }

        // Фоллбэк: пытаемся получить сущность, изменить и сохранить
        try {
            Optional<Car> existing = carRepository.findById(carId);
            if (existing.isPresent()) {
                Car car = existing.get();
                car.setStatus(newStatus);
                Car updated = carRepository.update(car);
                return updated != null;
            }
        } catch (Exception ignored) {
            // вернуть false, если не удалось
        }
        return false;
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

    public List<Car> getPopularCars(int limit) {
        return carRepository.getPopularCars(limit);
    }

    /**
     * Initializes all cars without coordinates with random locations in Kyiv
     */
    @PostConstruct
    public void initializeRandomLocations() {
        List<Car> cars = carRepository.findAll();
        for (Car car : cars) {
            if (car.getLatitude() == null || car.getLongitude() == null) {
                // Kyiv bounds
                double lat = 50.3500 + (Math.random() * (50.5500 - 50.3500));
                double lng = 30.3500 + (Math.random() * (30.7500 - 30.3500));
                car.setLatitude(lat);
                car.setLongitude(lng);
                carRepository.update(car);
            }
        }
    }
}