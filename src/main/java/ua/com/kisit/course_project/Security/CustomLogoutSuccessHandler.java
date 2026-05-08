package ua.com.kisit.course_project.Security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ua.com.kisit.course_project.Service.AuditService;

public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

    private final AuditService auditService;

    public CustomLogoutSuccessHandler(AuditService auditService) {
        this.auditService = auditService;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        String user = (authentication != null && authentication.getName() != null) ? authentication.getName() : "anonymous";
        auditService.record(user, "LOGOUT", "User logged out");
        response.sendRedirect("/");
    }
}
