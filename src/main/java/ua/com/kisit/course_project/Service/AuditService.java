package ua.com.kisit.course_project.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import ua.com.kisit.course_project.Entity.AuditLog;
import ua.com.kisit.course_project.Repository.AuditLogRepository;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final Logger logger = LoggerFactory.getLogger(AuditService.class);

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String userEmail, String action, String detail) {
        String ip = resolveClientIp();
        try {
            AuditLog log = new AuditLog(userEmail, action, detail, ip);
            auditLogRepository.save(log);
        } catch (Exception e) {
            logger.error("Failed to save audit log: {}", e.getMessage());
        }
        logger.info("AUDIT user={} action={} ip={} detail={}", userEmail, action, ip, detail);
    }

    private String resolveClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return "unknown";
            HttpServletRequest request = attrs.getRequest();
            String xf = request.getHeader("X-Forwarded-For");
            if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
            return request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
