package ua.com.kisit.course_project.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity class representing a car in the rental system
 */
public class Car {
    private Long carId;
    private String brand;
    private String model;
    private Integer year;
    private String color;
    private String registrationNumber;
    private String vinCode;
    private TransmissionType transmissionType;
    private FuelType fuelType;
    private Integer seatsCount;
    private BigDecimal dailyRate;
    private CarStatus status;
    private Integer mileage;
    private String imageUrl;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Enums
    public enum TransmissionType {
        MANUAL("Механічна"),
        AUTOMATIC("Автоматична"),
        ROBOT("Роботизована");

        private final String displayName;

        TransmissionType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum FuelType {
        PETROL("Бензин"),
        DIESEL("Дизель"),
        ELECTRIC("Електро"),
        HYBRID("Гібрид");

        private final String displayName;

        FuelType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum CarStatus {
        AVAILABLE("Доступний"),
        RENTED("Орендований"),
        MAINTENANCE("На обслуговуванні"),
        DAMAGED("Пошкоджений");

        private final String displayName;

        CarStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructors
    public Car() {
    }

    public Car(String brand, String model, Integer year, String registrationNumber,
               TransmissionType transmissionType, FuelType fuelType, Integer seatsCount,
               BigDecimal dailyRate) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.registrationNumber = registrationNumber;
        this.transmissionType = transmissionType;
        this.fuelType = fuelType;
        this.seatsCount = seatsCount;
        this.dailyRate = dailyRate;
        this.status = CarStatus.AVAILABLE;
        this.mileage = 0;
    }

    // Getters and Setters
    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getVinCode() {
        return vinCode;
    }

    public void setVinCode(String vinCode) {
        this.vinCode = vinCode;
    }

    public TransmissionType getTransmissionType() {
        return transmissionType;
    }

    public void setTransmissionType(TransmissionType transmissionType) {
        this.transmissionType = transmissionType;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Integer getSeatsCount() {
        return seatsCount;
    }

    public void setSeatsCount(Integer seatsCount) {
        this.seatsCount = seatsCount;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public void setDailyRate(BigDecimal dailyRate) {
        this.dailyRate = dailyRate;
    }

    public CarStatus getStatus() {
        return status;
    }

    public void setStatus(CarStatus status) {
        this.status = status;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
    public String getFullName() {
        return brand + " " + model + " " + year;
    }

    public boolean isAvailable() {
        return status == CarStatus.AVAILABLE;
    }

    @Override
    public String toString() {
        return "Car{" +
                "carId=" + carId +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year=" + year +
                ", registrationNumber='" + registrationNumber + '\'' +
                ", status=" + status +
                ", dailyRate=" + dailyRate +
                '}';
    }
}