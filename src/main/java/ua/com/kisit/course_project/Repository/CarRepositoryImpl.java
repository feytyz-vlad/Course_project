package ua.com.kisit.course_project.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Car.*;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository  // FIXED: додана анотація + замінено Connection → JdbcTemplate
public class CarRepositoryImpl implements CarRepository {

    private final JdbcTemplate jdbcTemplate;

    // FIXED: конструктор тепер приймає JdbcTemplate який Spring вміє autowire
    public CarRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // RowMapper — перетворює рядок ResultSet у об'єкт Car
    private final RowMapper<Car> carRowMapper = (rs, rowNum) -> {
        Car car = new Car();
        car.setCarId(rs.getLong("car_id"));
        car.setBrand(rs.getString("brand"));
        car.setModel(rs.getString("model"));
        car.setYear(rs.getInt("year"));
        car.setColor(rs.getString("color"));
        car.setRegistrationNumber(rs.getString("registration_number"));
        car.setVinCode(rs.getString("vin_code"));
        String transStr = rs.getString("transmission_type");
        if (transStr != null) {
            try {
                car.setTransmissionType(TransmissionType.valueOf(transStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                car.setTransmissionType(TransmissionType.MANUAL);
            }
        }
        
        String fuelStr = rs.getString("fuel_type");
        if (fuelStr != null) {
            try {
                car.setFuelType(FuelType.valueOf(fuelStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                car.setFuelType(FuelType.PETROL);
            }
        }
        
        car.setSeatsCount(rs.getInt("seats_count"));
        car.setDailyRate(rs.getBigDecimal("daily_rate"));
        
        String statusStr = rs.getString("status");
        if (statusStr != null) {
            try {
                car.setStatus(CarStatus.valueOf(statusStr.toUpperCase()));
            } catch (IllegalArgumentException e) {
                car.setStatus(CarStatus.AVAILABLE);
            }
        } else {
            car.setStatus(CarStatus.AVAILABLE);
        }
        car.setMileage(rs.getInt("mileage"));
        car.setImageUrl(rs.getString("image_url"));
        car.setDescription(rs.getString("description"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) car.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) car.setUpdatedAt(updatedAt.toLocalDateTime());
        return car;
    };

    @Override
    public Optional<Car> findById(Long carId) {
        String sql = "SELECT * FROM cars WHERE car_id = ?";
        List<Car> result = jdbcTemplate.query(sql, carRowMapper, carId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Car> findByRegistrationNumber(String registrationNumber) {
        String sql = "SELECT * FROM cars WHERE registration_number = ?";
        List<Car> result = jdbcTemplate.query(sql, carRowMapper, registrationNumber);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Car> findByVinCode(String vinCode) {
        String sql = "SELECT * FROM cars WHERE vin_code = ?";
        List<Car> result = jdbcTemplate.query(sql, carRowMapper, vinCode);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Car save(Car car) {
        String sql = "INSERT INTO cars (brand, model, year, color, registration_number, vin_code, " +
                "transmission_type, fuel_type, seats_count, daily_rate, status, mileage, image_url, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, car.getBrand());
            ps.setString(2, car.getModel());
            ps.setInt(3, car.getYear());
            ps.setString(4, car.getColor());
            ps.setString(5, car.getRegistrationNumber());
            ps.setString(6, (car.getVinCode() != null && !car.getVinCode().isBlank()) ? car.getVinCode() : null);
            ps.setString(7, car.getTransmissionType() != null ? car.getTransmissionType().name() : TransmissionType.MANUAL.name());
            ps.setString(8, car.getFuelType() != null ? car.getFuelType().name() : FuelType.PETROL.name());
            ps.setInt(9, car.getSeatsCount());
            ps.setBigDecimal(10, car.getDailyRate());
            ps.setString(11, car.getStatus() != null ? car.getStatus().name() : CarStatus.AVAILABLE.name());
            ps.setInt(12, car.getMileage() != null ? car.getMileage() : 0);
            ps.setString(13, car.getImageUrl());
            ps.setString(14, car.getDescription());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            car.setCarId(keyHolder.getKey().longValue());
        }
        return car;
    }

    @Override
    public Car update(Car car) {
        String sql = "UPDATE cars SET brand=?, model=?, year=?, color=?, registration_number=?, " +
                "vin_code=?, transmission_type=?, fuel_type=?, seats_count=?, daily_rate=?, " +
                "status=?, mileage=?, image_url=?, description=? WHERE car_id=?";

        jdbcTemplate.update(sql,
                car.getBrand(), car.getModel(), car.getYear(), car.getColor(),
                car.getRegistrationNumber(),
                (car.getVinCode() != null && !car.getVinCode().isBlank()) ? car.getVinCode() : null,
                car.getTransmissionType() != null ? car.getTransmissionType().name() : TransmissionType.MANUAL.name(), 
                car.getFuelType() != null ? car.getFuelType().name() : FuelType.PETROL.name(),
                car.getSeatsCount(), car.getDailyRate(), car.getStatus().name(),
                car.getMileage(), car.getImageUrl(), car.getDescription(),
                car.getCarId());
        return car;
    }

    @Override
    public boolean deleteById(Long carId) {
        return jdbcTemplate.update("DELETE FROM cars WHERE car_id=?", carId) > 0;
    }

    @Override
    public List<Car> findAll() {
        return jdbcTemplate.query("SELECT * FROM cars ORDER BY brand, model", carRowMapper);
    }

    @Override
    public List<Car> findAllPaginated(int limit, int offset) {
        return jdbcTemplate.query("SELECT * FROM cars ORDER BY brand, model LIMIT ? OFFSET ?",
                carRowMapper, limit, offset);
    }

    @Override
    public List<Car> findAvailablePaginated(int limit, int offset) {
        return jdbcTemplate.query("SELECT * FROM cars WHERE status='AVAILABLE' ORDER BY brand, model LIMIT ? OFFSET ?",
                carRowMapper, limit, offset);
    }

    @Override
    public List<Car> findByStatus(CarStatus status) {
        return jdbcTemplate.query("SELECT * FROM cars WHERE status=? ORDER BY brand, model",
                carRowMapper, status.name());
    }

    @Override
    public List<Car> findAvailableCars() {
        return findByStatus(CarStatus.AVAILABLE);
    }

    @Override
    public List<Car> findByBrand(String brand) {
        return jdbcTemplate.query("SELECT * FROM cars WHERE brand=? ORDER BY model, year DESC",
                carRowMapper, brand);
    }

    @Override
    public List<Car> findByTransmissionType(TransmissionType transmissionType) {
        return jdbcTemplate.query("SELECT * FROM cars WHERE transmission_type=? ORDER BY brand, model",
                carRowMapper, transmissionType.name());
    }

    @Override
    public List<Car> findByFuelType(FuelType fuelType) {
        return jdbcTemplate.query("SELECT * FROM cars WHERE fuel_type=? ORDER BY brand, model",
                carRowMapper, fuelType.name());
    }

    @Override
    public List<Car> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return jdbcTemplate.query("SELECT * FROM cars WHERE daily_rate BETWEEN ? AND ? ORDER BY daily_rate",
                carRowMapper, minPrice, maxPrice);
    }

    @Override
    public List<Car> searchCars(String brand, CarStatus status, TransmissionType transmission,
                                FuelType fuel, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder("SELECT * FROM cars WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (brand != null && !brand.isEmpty()) { sql.append(" AND brand=?"); params.add(brand); }
        if (status != null)       { sql.append(" AND status=?");            params.add(status.name()); }
        if (transmission != null) { sql.append(" AND transmission_type=?"); params.add(transmission.name()); }
        if (fuel != null)         { sql.append(" AND fuel_type=?");         params.add(fuel.name()); }
        if (maxPrice != null)     { sql.append(" AND daily_rate<=?");       params.add(maxPrice); }
        sql.append(" ORDER BY brand, model");

        return jdbcTemplate.query(sql.toString(), carRowMapper, params.toArray());
    }

    @Override
    public boolean updateStatus(Long carId, CarStatus newStatus) {
        return jdbcTemplate.update("UPDATE cars SET status=? WHERE car_id=?",
                newStatus.name(), carId) > 0;
    }

    @Override
    public boolean updateMileage(Long carId, Integer newMileage) {
        return jdbcTemplate.update("UPDATE cars SET mileage=? WHERE car_id=?",
                newMileage, carId) > 0;
    }

    @Override
    public boolean existsByRegistrationNumber(String registrationNumber) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cars WHERE registration_number=?", Integer.class, registrationNumber);
        return count != null && count > 0;
    }

    @Override
    public long countAll() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM cars", Long.class);
        return count != null ? count : 0;
    }

    @Override
    public long countAvailable() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM cars WHERE status='AVAILABLE'", Long.class);
        return count != null ? count : 0;
    }
}