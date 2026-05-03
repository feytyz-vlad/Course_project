package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Car.CarStatus;
import ua.com.kisit.course_project.Entity.Car.FuelType;
import ua.com.kisit.course_project.Entity.Car.TransmissionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Car entity
 */
public interface CarRepository {

    /**
     * Find car by ID
     */
    Optional<Car> findById(Long carId);

    /**
     * Find car by registration number
     */
    Optional<Car> findByRegistrationNumber(String registrationNumber);

    /**
     * Find car by VIN code
     */
    Optional<Car> findByVinCode(String vinCode);

    /**
     * Save new car
     */
    Car save(Car car);

    /**
     * Update existing car
     */
    Car update(Car car);

    /**
     * Delete car by ID
     */
    boolean deleteById(Long carId);

    /**
     * Find all cars
     */
    List<Car> findAll();

    /**
     * Find cars by status
     */
    List<Car> findByStatus(CarStatus status);

    /**
     * Find all cars with pagination
     */
    List<Car> findAllPaginated(int limit, int offset);

    /**
     * Find available cars with pagination
     */
    List<Car> findAvailablePaginated(int limit, int offset);

    /**
     * Find available cars
     */
    List<Car> findAvailableCars();

    /**
     * Find cars by brand
     */
    List<Car> findByBrand(String brand);

    /**
     * Find cars by transmission type
     */
    List<Car> findByTransmissionType(TransmissionType transmissionType);

    /**
     * Find cars by fuel type
     */
    List<Car> findByFuelType(FuelType fuelType);

    /**
     * Find cars by price range
     */
    List<Car> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Search cars by multiple criteria
     */
    List<Car> searchCars(String brand, CarStatus status, TransmissionType transmission,
                         FuelType fuel, BigDecimal maxPrice);

    /**
     * Update car status
     */
    boolean updateStatus(Long carId, CarStatus newStatus);

    /**
     * Update car mileage
     */
    boolean updateMileage(Long carId, Integer newMileage);

    /**
     * Check if registration number exists
     */
    boolean existsByRegistrationNumber(String registrationNumber);

    /**
     * Get total cars count
     */
    long countAll();

    /**
     * Get available cars count
     */
    long countAvailable();
}