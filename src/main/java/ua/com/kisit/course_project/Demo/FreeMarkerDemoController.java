package ua.com.kisit.course_project.Demo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// imports removed — demo no longer constructs example data

@Controller
public class FreeMarkerDemoController {

    @GetMapping("/fm/home")
    public String fmHome(Model model) {
        // Demo endpoint removed to avoid colliding with real "home" view.
        // Redirect to main home so the site renders using the real Car entities.
        return "redirect:/";
    }
}
