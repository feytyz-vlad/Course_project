package ua.com.kisit.course_project.Controller.Web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

	// Добавляет isAuthenticated, userEmail и userRoles в модель для всех контроллеров
	@ModelAttribute
	public void addUserAttributes(Model model, Authentication authentication) {
		boolean isAuthenticated = authentication != null
				&& authentication.isAuthenticated()
				&& !(authentication instanceof AnonymousAuthenticationToken);

		model.addAttribute("isAuthenticated", isAuthenticated);

		if (isAuthenticated) {
			model.addAttribute("userEmail", authentication.getName());
			model.addAttribute("userRoles", authentication.getAuthorities());
		}
	}
}
