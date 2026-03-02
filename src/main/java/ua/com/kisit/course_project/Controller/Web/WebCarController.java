package ua.com.kisit.course_project.Controller.Web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Car.*;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Service.CarService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/cars")
public class WebCarController {

    private final CarService carService;

    public WebCarController(CarService carService) {
        this.carService = carService;
    }

    private void addEnumsToModel(Model model) {
        model.addAttribute("transmissionTypes", TransmissionType.values());
        model.addAttribute("fuelTypes", FuelType.values());
        model.addAttribute("carStatuses", CarStatus.values());
    }

    @GetMapping
    public String listCars(Model model) {
        model.addAttribute("cars", carService.getAllCars());
        model.addAttribute("title", "Всі автомобілі");
        addEnumsToModel(model);
        return "cars/list";
    }

    @GetMapping("/available")
    public String availableCars(Model model) {
        model.addAttribute("cars", carService.getAvailableCars());
        model.addAttribute("title", "Доступні автомобілі");
        addEnumsToModel(model);
        return "cars/list";
    }

    // FIXED: enum параметри як String щоб уникнути 400 Bad Request
    // при порожніх значеннях зі select-форми
    @GetMapping("/search")
    public String searchCars(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String transmission,
            @RequestParam(required = false) String fuel,
            @RequestParam(required = false) BigDecimal maxPrice,
            Model model) {

        TransmissionType transmissionEnum = parseEnum(TransmissionType.class, transmission);
        FuelType fuelEnum = parseEnum(FuelType.class, fuel);

        List<Car> cars = carService.searchCars(brand, null, transmissionEnum, fuelEnum, maxPrice);

        model.addAttribute("cars", cars);
        model.addAttribute("title", "Результати пошуку");
        model.addAttribute("searchBrand", brand);
        model.addAttribute("searchTransmission", transmissionEnum);
        model.addAttribute("searchFuel", fuelEnum);
        model.addAttribute("searchMaxPrice", maxPrice);
        addEnumsToModel(model);

        return "cars/list";
    }

    @GetMapping("/{id}")
    public String carDetails(@PathVariable Long id, Model model) {
        Optional<Car> carOpt = carService.getCarById(id);
        if (carOpt.isEmpty()) return "redirect:/cars/available";
        model.addAttribute("car", carOpt.get());
        return "cars/details";
    }

    @GetMapping("/add")
    public String showAddForm(HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/cars/available";
        model.addAttribute("car", new Car());
        addEnumsToModel(model);
        return "cars/form";
    }

    @PostMapping("/add")
    public String addCar(@ModelAttribute Car car, HttpSession session,
                         RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) {
            redirectAttributes.addFlashAttribute("error", "Доступ заборонено!");
            return "redirect:/cars/available";
        }
        try {
            Car saved = carService.addCar(car);
            redirectAttributes.addFlashAttribute("success", "Автомобіль успішно додано!");
            return "redirect:/cars/" + saved.getCarId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cars/add";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, HttpSession session, Model model) {
        if (!isAdmin(session)) return "redirect:/cars/" + id;
        Optional<Car> carOpt = carService.getCarById(id);
        if (carOpt.isEmpty()) return "redirect:/cars/available";
        model.addAttribute("car", carOpt.get());
        addEnumsToModel(model);
        return "cars/form";
    }

    @PostMapping("/{id}/edit")
    public String editCar(@PathVariable Long id, @ModelAttribute Car car,
                          HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/cars/" + id;
        try {
            car.setCarId(id);
            carService.updateCar(car);
            redirectAttributes.addFlashAttribute("success", "Автомобіль оновлено!");
            return "redirect:/cars/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cars/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteCar(@PathVariable Long id, HttpSession session,
                            RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/cars/" + id;
        try {
            carService.deleteCar(id);
            redirectAttributes.addFlashAttribute("success", "Автомобіль видалено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cars/" + id;
        }
        return "redirect:/cars";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status,
                               HttpSession session, RedirectAttributes redirectAttributes) {
        if (!isAdmin(session)) return "redirect:/cars/" + id;
        try {
            carService.updateCarStatus(id, CarStatus.valueOf(status));
            redirectAttributes.addFlashAttribute("success", "Статус оновлено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cars/" + id;
    }

    private boolean isAdmin(HttpSession session) {
        UserRole role = (UserRole) session.getAttribute("userRole");
        return role == UserRole.ADMIN;
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumClass, String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}