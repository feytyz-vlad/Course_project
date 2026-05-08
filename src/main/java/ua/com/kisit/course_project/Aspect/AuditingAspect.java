package ua.com.kisit.course_project.Aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import ua.com.kisit.course_project.Annotation.Auditable;
import ua.com.kisit.course_project.Service.AuditService;

@Aspect
@Component
public class AuditingAspect {

    private final AuditService auditService;

    public AuditingAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(ua.com.kisit.course_project.Annotation.Auditable)")
    public Object aroundAuditable(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        Auditable aud = sig.getMethod().getAnnotation(Auditable.class);
        String action = aud != null && !aud.action().isEmpty() ? aud.action() : sig.getMethod().getName();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String user = (auth != null && auth.isAuthenticated() && auth.getName() != null) ? auth.getName() : "anonymous";

        String args = "";
        try {
            Object[] a = pjp.getArgs();
            if (a != null && a.length > 0) {
                StringBuilder sb = new StringBuilder();
                for (Object o : a) {
                    if (o == null) continue;
                    sb.append(o.getClass().getSimpleName()).append(":").append(o.toString()).append("; ");
                }
                args = sb.toString();
            }
        } catch (Exception ignored) {}

        auditService.record(user, action + ".started", args);
        try {
            Object result = pjp.proceed();
            auditService.record(user, action + ".success", args);
            return result;
        } catch (Throwable t) {
            auditService.record(user, action + ".failed", t.getMessage());
            throw t;
        }
    }
}
