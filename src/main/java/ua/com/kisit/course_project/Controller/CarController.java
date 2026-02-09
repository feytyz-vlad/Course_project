package ua.com.kisit.course_project.Controller;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Car.*;
import ua.com.kisit.course_project.Service.CarService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CarController {
    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    public Car addCar(Car car) {
        try {
            return carService.addCar(car);
        } catch (IllegalArgumentException e) {
            System.err.println("Error adding car: " + e.getMessage());
            return null;
        }
    }

    public Car updateCar(Car car) {
        try {
            return carService.updateCar(car);
        } catch (IllegalArgumentException e) {
            System.err.println("Error updating car: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteCar(Long carId) {
        return carService.deleteCar(carId);
    }

    public Optional<Car> getCarById(Long carId) {
        return carService.getCarById(carId);
    }

    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    public List<Car> getAvailableCars() {
        return carService.getAvailableCars();
    }

    public List<Car> searchCars(String brand, CarStatus status, TransmissionType transmission,
                                FuelType fuel, BigDecimal maxPrice) {
        return carService.searchCars(brand, status, transmission, fuel, maxPrice);
    }

    public void displayCarList(List<Car> cars) {
        System.out.println("\n=== Список автомобілів ===");
        if (cars.isEmpty()) {
            System.out.println("Автомобілів не знайдено");
            return;
        }

        for (Car car : cars) {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("ID: " + car.getCarId());
            System.out.println("Автомобіль: " + car.getFullName());
            System.out.println("Номер: " + car.getRegistrationNumber());
            System.out.println("Коробка передач: " + car.getTransmissionType().getDisplayName());
            System.out.println("Паливо: " + car.getFuelType().getDisplayName());
            System.out.println("Ціна за день: " + car.getDailyRate() + " грн");
            System.out.println("Статус: " + car.getStatus().getDisplayName());
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}