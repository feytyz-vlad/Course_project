package ua.com.kisit.course_project.Controller;

import ua.com.kisit.course_project.Entity.DamageReport;
import ua.com.kisit.course_project.Service.DamageReportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DamageReportController {
    private final DamageReportService damageReportService;

    public DamageReportController(DamageReportService damageReportService) {
        this.damageReportService = damageReportService;
    }

    public DamageReport createReport(Long orderId, Long carId, String description,
                                     LocalDate damageDate, Long createdByUserId) {
        return damageReportService.createReport(orderId, carId, description, damageDate, createdByUserId);
    }

    public boolean setRepairCost(Long reportId, BigDecimal cost) {
        try {
            return damageReportService.setRepairCost(reportId, cost);
        } catch (IllegalArgumentException e) {
            System.err.println("Error setting repair cost: " + e.getMessage());
            return false;
        }
    }

    public boolean markAsInRepair(Long reportId) {
        return damageReportService.markAsInRepair(reportId);
    }

    public boolean markAsCompleted(Long reportId) {
        try {
            return damageReportService.markAsCompleted(reportId);
        } catch (IllegalArgumentException e) {
            System.err.println("Error completing repair: " + e.getMessage());
            return false;
        }
    }

    public List<DamageReport> getReportsByCarId(Long carId) {
        return damageReportService.getReportsByCarId(carId);
    }

    public List<DamageReport> getAllReports() {
        return damageReportService.getAllReports();
    }

    public void displayReportList(List<DamageReport> reports) {
        System.out.println("\n=== Звіти про пошкодження ===");
        if (reports.isEmpty()) {
            System.out.println("Звітів не знайдено");
            return;
        }

        for (DamageReport report : reports) {
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println("ID звіту: " + report.getReportId());
            System.out.println("Дата пошкодження: " + report.getDamageDate());
            System.out.println("Опис: " + report.getDamageDescription());
            System.out.println("Вартість ремонту: " + report.getRepairCost() + " грн");
            System.out.println("Статус: " + report.getRepairStatus().getDisplayName());
        }
        System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }
}