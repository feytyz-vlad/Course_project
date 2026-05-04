package ua.com.kisit.course_project.Entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity class representing a damage report for a car
 */
public class DamageReport {
    private Long reportId;
    private Long orderId;
    private Long carId;
    private String damageDescription;
    private LocalDate damageDate;
    private BigDecimal repairCost;
    private BigDecimal fineAmount;
    private RepairStatus repairStatus;
    private String photosUrl;
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enum for repair status
    public enum RepairStatus {
        ASSESSED("Оцінено"),
        IN_REPAIR("В ремонті"),
        COMPLETED("Завершено"),
        PAID("Оплачено");

        private final String displayName;

        RepairStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public DamageReport() {
    }

    public DamageReport(Long orderId, Long carId, String damageDescription,
                        LocalDate damageDate, Long createdByUserId) {
        this.orderId = orderId;
        this.carId = carId;
        this.damageDescription = damageDescription;
        this.damageDate = damageDate;
        this.createdByUserId = createdByUserId;
        this.repairStatus = RepairStatus.ASSESSED;
    }

    // Getters and Setters
    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getDamageDescription() {
        return damageDescription;
    }

    public void setDamageDescription(String damageDescription) {
        this.damageDescription = damageDescription;
    }

    public LocalDate getDamageDate() {
        return damageDate;
    }

    public void setDamageDate(LocalDate damageDate) {
        this.damageDate = damageDate;
    }

    public BigDecimal getRepairCost() {
        return repairCost;
    }

    public void setRepairCost(BigDecimal repairCost) {
        this.repairCost = repairCost;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount;
    }

    public RepairStatus getRepairStatus() {
        return repairStatus;
    }

    public void setRepairStatus(RepairStatus repairStatus) {
        this.repairStatus = repairStatus;
    }

    public String getPhotosUrl() {
        return photosUrl;
    }

    public void setPhotosUrl(String photosUrl) {
        this.photosUrl = photosUrl;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Helper methods
    public boolean isAssessed() {
        return repairStatus == RepairStatus.ASSESSED;
    }

    public boolean isInRepair() {
        return repairStatus == RepairStatus.IN_REPAIR;
    }

    public boolean isCompleted() {
        return repairStatus == RepairStatus.COMPLETED;
    }

    public boolean isPaid() {
        return repairStatus == RepairStatus.PAID;
    }

    public boolean hasRepairCost() {
        return repairCost != null && repairCost.compareTo(BigDecimal.ZERO) > 0;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal total = BigDecimal.ZERO;
        if (repairCost != null) total = total.add(repairCost);
        if (fineAmount != null) total = total.add(fineAmount);
        return total;
    }

    @Override
    public String toString() {
        return "DamageReport{" +
                "reportId=" + reportId +
                ", orderId=" + orderId +
                ", carId=" + carId +
                ", damageDate=" + damageDate +
                ", repairCost=" + repairCost +
                ", repairStatus=" + repairStatus +
                '}';
    }
}