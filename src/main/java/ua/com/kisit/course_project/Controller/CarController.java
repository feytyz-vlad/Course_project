package ua.com.kisit.course_project.Controller;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Service.CarService;

@Controller
@RequestMapping("/cars")
public class CarController {
    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping("/available")
    public String listAvailableCars(Model model) {
        List<Car> cars = carService.getAvailableCars();
        model.addAttribute("cars", cars);
        model.addAttribute("totalCars", carService.getTotalCarsCount());
        return "cars/list";
    }

    @GetMapping("/{id}")
    public String showCarDetails(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Car> carOpt = carService.getCarById(id);
        if (carOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Автомобіль не знайдено");
            return "redirect:/cars/available";
        }
        model.addAttribute("car", carOpt.get());
        return "cars/detail";
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

    public List<Car> searchCars(String brand, Car.CarStatus status, Car.TransmissionType transmission,
                                Car.FuelType fuel, java.math.BigDecimal maxPrice) {
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