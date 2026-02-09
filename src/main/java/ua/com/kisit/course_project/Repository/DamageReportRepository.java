package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.DamageReport;
import ua.com.kisit.course_project.Entity.DamageReport.RepairStatus;
import java.util.List;
import java.util.Optional;

public interface DamageReportRepository {
    Optional<DamageReport> findById(Long reportId);
    DamageReport save(DamageReport report);
    DamageReport update(DamageReport report);
    boolean deleteById(Long reportId);
    List<DamageReport> findAll();
    List<DamageReport> findByOrderId(Long orderId);
    List<DamageReport> findByCarId(Long carId);
    List<DamageReport> findByStatus(RepairStatus status);
    boolean updateStatus(Long reportId, RepairStatus newStatus);
}