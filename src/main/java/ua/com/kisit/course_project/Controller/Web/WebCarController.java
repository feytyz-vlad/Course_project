package ua.com.kisit.course_project.Controller.Web;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Car.CarStatus;
import ua.com.kisit.course_project.Entity.Car.FuelType;
import ua.com.kisit.course_project.Entity.Car.TransmissionType;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Service.CarService;

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
        model.addAttribute("carClasses", Car.CarClass.values());
    }

    @GetMapping
    public String listCars(@RequestParam(defaultValue = "0") int page, Model model) {
        int size = 9;
        model.addAttribute("cars", carService.getAllCarsPaginated(page, size));
        model.addAttribute("title", "Всі автомобілі");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) carService.getTotalCarsCount() / size));
        model.addAttribute("baseUrl", "/cars");
        addEnumsToModel(model);
        return "cars/list";
    }

    @GetMapping("/available")
    public String availableCars(@RequestParam(defaultValue = "0") int page, Model model) {
        int size = 9;
        model.addAttribute("cars", carService.getAvailableCarsPaginated(page, size));
        model.addAttribute("title", "Доступні автомобілі");
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) carService.getAvailableCarsCount() / size));
        model.addAttribute("baseUrl", "/cars/available");
        addEnumsToModel(model);
        return "cars/list";
    }

    @GetMapping("/search")
    public String searchCars(
            @RequestParam(required = false) String query,
            @RequestParam(required = false, name = "class") String carClass,
            @RequestParam(required = false) String fuel,
            @RequestParam(required = false) Integer seats,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortOrder,
            Model model) {

        Car.CarClass classEnum = parseEnum(Car.CarClass.class, carClass);
        FuelType fuelEnum = parseEnum(FuelType.class, fuel);

        List<Car> cars = carService.searchCars(query, classEnum, fuelEnum, seats, year, minPrice, maxPrice, sortOrder);

        model.addAttribute("cars", cars);
        model.addAttribute("title", "Результати пошуку");
        model.addAttribute("searchQuery", query);
        model.addAttribute("searchClass", classEnum);
        model.addAttribute("searchFuel", fuelEnum);
        model.addAttribute("searchSeats", seats);
        model.addAttribute("searchYear", year);
        model.addAttribute("searchMinPrice", minPrice);
        model.addAttribute("searchMaxPrice", maxPrice);
        model.addAttribute("searchSortOrder", sortOrder);
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
    public String showAddForm(Model model) {
        if (!canManageCars()) return "redirect:/cars/available";
        model.addAttribute("car", new Car());
        addEnumsToModel(model);
        return "cars/form";
    }

    @PostMapping("/add")
    public String addCar(@ModelAttribute Car car,
                         RedirectAttributes redirectAttributes) {
        if (!canManageCars()) {
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
    public String showEditForm(@PathVariable Long id, Model model) {
        if (!canManageCars()) return "redirect:/cars/" + id;
        Optional<Car> carOpt = carService.getCarById(id);
        if (carOpt.isEmpty()) return "redirect:/cars/available";
        model.addAttribute("car", carOpt.get());
        addEnumsToModel(model);
        return "cars/form";
    }

    @PostMapping("/{id}/edit")
    public String editCar(@PathVariable Long id, @ModelAttribute Car car,
                          RedirectAttributes redirectAttributes) {
        if (!canManageCars()) return "redirect:/cars/" + id;
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
    public String deleteCar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (!isAdmin()) return "redirect:/cars/" + id;
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
                               RedirectAttributes redirectAttributes) {
        if (!canManageCars()) return "redirect:/cars/" + id;
        try {
            carService.updateCarStatus(id, CarStatus.valueOf(status));
            redirectAttributes.addFlashAttribute("success", "Статус оновлено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cars/" + id;
    }

    private boolean canManageCars() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a -> 
            a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
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