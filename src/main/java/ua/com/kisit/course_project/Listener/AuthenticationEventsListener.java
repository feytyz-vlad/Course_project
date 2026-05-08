package ua.com.kisit.course_project.Listener;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import ua.com.kisit.course_project.Service.AuditService;

@Component
public class AuthenticationEventsListener {

    private final AuditService auditService;

    public AuthenticationEventsListener(AuditService auditService) {
        this.auditService = auditService;
    }

    @EventListener
    public void onAuthSuccess(AuthenticationSuccessEvent event) {
        String user = event.getAuthentication().getName();
        auditService.record(user, "AUTH_SUCCESS", "User logged in");
    }

    @EventListener
    public void onAuthFailure(AbstractAuthenticationFailureEvent event) {
        String principal = (event.getAuthentication() != null && event.getAuthentication().getName() != null)
                ? event.getAuthentication().getName() : "unknown";
        auditService.record(principal, "AUTH_FAILURE", event.getException().getMessage());
    }
}
