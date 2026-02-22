package ua.com.kisit.course_project.Controller.Web;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.UserRole;
import ua.com.kisit.course_project.Service.CarService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Web Controller for Cars
 */
@Controller
@RequestMapping("/cars")
public class WebCarController {

    private final CarService carService;

    public WebCarController(CarService carService) {
        this.carService = carService;
    }

    /**
     * Show all cars
     */
    @GetMapping
    public String listCars(Model model) {
        List<Car> cars = carService.getAllCars();
        model.addAttribute("cars", cars);
        return "cars/list";
    }

    /**
     * Show car details
     */
    @GetMapping("/{id}")
    public String carDetails(@PathVariable Long id, Model model) {
        Optional<Car> car = carService.getCarById(id);
        if (car.isEmpty()) {
            return "redirect:/cars";
        }
        model.addAttribute("car", car.get());
        return "cars/details";
    }

    /**
     * Show create car form (admin only)
     */
    @GetMapping("/add")
    public String showAddForm(HttpSession session) {
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        if (userRole != UserRole.ADMIN) {
            return "redirect:/";
        }
        return "cars/add";
    }

    /**
     * Create new car (admin only)
     */
    @PostMapping("/add")
    public String addCar(@ModelAttribute Car car,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        if (userRole != UserRole.ADMIN) {
            return "redirect:/";
        }

        try {
            carService.addCar(car);
            redirectAttributes.addFlashAttribute("success", "Автомобіль додано успішно!");
            return "redirect:/cars";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/cars/add";
        }
    }

    /**
     * Show edit car form (admin only)
     */
    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        if (userRole != UserRole.ADMIN) {
            return "redirect:/";
        }

        Optional<Car> car = carService.getCarById(id);
        if (car.isEmpty()) {
            return "redirect:/cars";
        }
        model.addAttribute("car", car.get());
        return "cars/edit";
    }

    /**
     * Update car (admin only)
     */
    @PostMapping("/{id}/edit")
    public String updateCar(@PathVariable Long id,
                            @ModelAttribute Car car,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        if (userRole != UserRole.ADMIN) {
            return "redirect:/";
        }

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

    /**
     * Delete car (admin only)
     */
    @PostMapping("/{id}/delete")
    public String deleteCar(@PathVariable Long id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        UserRole userRole = (UserRole) session.getAttribute("userRole");
        if (userRole != UserRole.ADMIN) {
            return "redirect:/";
        }

        try {
            carService.deleteCar(id);
            redirectAttributes.addFlashAttribute("success", "Автомобіль видалено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/cars";
    }

    /**
     * Search cars
     */
    @GetMapping("/search")
    public String searchCars(@RequestParam(required = false) String brand,
                             @RequestParam(required = false) BigDecimal maxPrice,
                             Model model) {
        List<Car> cars = carService.searchCars(brand, null, null, null, maxPrice);
        model.addAttribute("cars", cars);
        model.addAttribute("searchBrand", brand);
        model.addAttribute("searchMaxPrice", maxPrice);
        return "cars/list";
    }
}