package ua.com.kisit.course_project.Controller.Web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Legacy UI controller — перенаправляет старые пути на новый AdminController.
 * Это убирает конфликт с основным AdminController, который управляет /admin/*.
 */
@Controller
@RequestMapping("/admin-ui")
@PreAuthorize("hasRole('ADMIN')")
public class WebAdminController {

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        // перенаправление на основной контроллер
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/manage-users")
    public String manageUsers() {
        return "redirect:/admin/users";
    }

    @GetMapping("/settings")
    public String settings() {
        return "redirect:/admin/settings";
    }
}
