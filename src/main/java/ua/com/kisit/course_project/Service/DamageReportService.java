package ua.com.kisit.course_project.Service;

import org.springframework.stereotype.Service;
import ua.com.kisit.course_project.Entity.DamageReport;
import ua.com.kisit.course_project.Entity.DamageReport.RepairStatus;
import ua.com.kisit.course_project.Repository.DamageReportRepository;
import ua.com.kisit.course_project.Repository.CarRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service  // FIXED: додана анотація — без неї Spring не бачить цей клас як bean
public class DamageReportService {

    private final DamageReportRepository damageReportRepository;
    private final CarRepository carRepository;

    public DamageReportService(DamageReportRepository damageReportRepository, CarRepository carRepository) {
        this.damageReportRepository = damageReportRepository;
        this.carRepository = carRepository;
    }

    public DamageReport createReport(Long orderId, Long carId, String description,
                                     LocalDate damageDate, Long createdByUserId) {
        DamageReport report = new DamageReport(orderId, carId, description, damageDate, createdByUserId);
        DamageReport saved = damageReportRepository.save(report);

        if (saved != null) {
            carRepository.updateStatus(carId, ua.com.kisit.course_project.Entity.Car.CarStatus.DAMAGED);
        }
        return saved;
    }

    public boolean setRepairCost(Long reportId, BigDecimal cost) {
        Optional<DamageReport> reportOpt = damageReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new IllegalArgumentException("Звіт не знайдено");
        }

        DamageReport report = reportOpt.get();
        report.setRepairCost(cost);
        return damageReportRepository.update(report) != null;
    }

    public boolean markAsInRepair(Long reportId) {
        return damageReportRepository.updateStatus(reportId, RepairStatus.IN_REPAIR);
    }

    public boolean markAsCompleted(Long reportId) {
        Optional<DamageReport> reportOpt = damageReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new IllegalArgumentException("Звіт не знайдено");
        }

        DamageReport report = reportOpt.get();
        boolean updated = damageReportRepository.updateStatus(reportId, RepairStatus.COMPLETED);

        if (updated) {
            carRepository.updateStatus(report.getCarId(),
                    ua.com.kisit.course_project.Entity.Car.CarStatus.AVAILABLE);
        }
        return updated;
    }

    public List<DamageReport> getReportsByCarId(Long carId) {
        return damageReportRepository.findByCarId(carId);
    }

    public List<DamageReport> getAllReports() {
        return damageReportRepository.findAll();
    }
}