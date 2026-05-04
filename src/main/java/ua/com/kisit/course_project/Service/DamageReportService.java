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
                                     LocalDate damageDate, BigDecimal repairCost, BigDecimal fineAmount, Long createdByUserId) {
        DamageReport report = new DamageReport(orderId, carId, description, damageDate, createdByUserId);
        report.setRepairCost(repairCost);
        report.setFineAmount(fineAmount);
        DamageReport saved = damageReportRepository.save(report);

        if (saved != null) {
            carRepository.updateStatus(carId, ua.com.kisit.course_project.Entity.Car.CarStatus.DAMAGED);
        }
        return saved;
    }

    public boolean setRepairCosts(Long reportId, BigDecimal cost, BigDecimal fine) {
        Optional<DamageReport> reportOpt = damageReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new IllegalArgumentException("Звіт не знайдено");
        }

        DamageReport report = reportOpt.get();
        report.setRepairCost(cost);
        report.setFineAmount(fine);
        return damageReportRepository.update(report) != null;
    }

    public boolean markAsInRepair(Long reportId) {
        return damageReportRepository.updateStatus(reportId, RepairStatus.IN_REPAIR);
    }

    public boolean payDamage(Long reportId) {
        Optional<DamageReport> reportOpt = damageReportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new IllegalArgumentException("Звіт не знайдено");
        }

        DamageReport report = reportOpt.get();
        boolean updated = damageReportRepository.updateStatus(reportId, RepairStatus.PAID);
        
        if (updated) {
            // Якщо оплачено, зазвичай машина ще має пройти ремонт або вже готова
            // Для спрощення, якщо статус PAID, ми можемо перевести авто в AVAILABLE або MAINTENANCE
            // Користувач просив: "по завершению аренды... уведомление... пользователь проводит оплату"
            // Якщо оплата пройшла, ставимо статус авто назад в AVAILABLE (або адмін потім сам змінить якщо треба ремонт)
            carRepository.updateStatus(report.getCarId(), ua.com.kisit.course_project.Entity.Car.CarStatus.AVAILABLE);
        }
        return updated;
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

    public List<DamageReport> getReportsByOrderId(Long orderId) {
        return damageReportRepository.findByOrderId(orderId);
    }

    public List<DamageReport> getReportsByCarId(Long carId) {
        return damageReportRepository.findByCarId(carId);
    }

    public List<DamageReport> getAllReports() {
        return damageReportRepository.findAll();
    }
}