package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.Car;
import ua.com.kisit.course_project.Entity.Car.*;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CarRepositoryImpl implements CarRepository {

    private final Connection connection;

    public CarRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Car> findById(Long carId) {
        String sql = "SELECT * FROM cars WHERE car_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, carId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Car> findByRegistrationNumber(String registrationNumber) {
        String sql = "SELECT * FROM cars WHERE registration_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, registrationNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Car> findByVinCode(String vinCode) {
        String sql = "SELECT * FROM cars WHERE vin_code = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, vinCode);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Car save(Car car) {
        String sql = "INSERT INTO cars (brand, model, year, color, registration_number, vin_code, " +
                "transmission_type, fuel_type, seats_count, daily_rate, status, mileage, image_url, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, car.getBrand());
            stmt.setString(2, car.getModel());
            stmt.setInt(3, car.getYear());
            stmt.setString(4, car.getColor());
            stmt.setString(5, car.getRegistrationNumber());
            stmt.setString(6, car.getVinCode());
            stmt.setString(7, car.getTransmissionType().name());
            stmt.setString(8, car.getFuelType().name());
            stmt.setInt(9, car.getSeatsCount());
            stmt.setBigDecimal(10, car.getDailyRate());
            stmt.setString(11, car.getStatus().name());
            stmt.setInt(12, car.getMileage() != null ? car.getMileage() : 0);
            stmt.setString(13, car.getImageUrl());
            stmt.setString(14, car.getDescription());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    car.setCarId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return car;
    }

    @Override
    public Car update(Car car) {
        String sql = "UPDATE cars SET brand = ?, model = ?, year = ?, color = ?, " +
                "registration_number = ?, vin_code = ?, transmission_type = ?, fuel_type = ?, " +
                "seats_count = ?, daily_rate = ?, status = ?, mileage = ?, image_url = ?, " +
                "description = ? WHERE car_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, car.getBrand());
            stmt.setString(2, car.getModel());
            stmt.setInt(3, car.getYear());
            stmt.setString(4, car.getColor());
            stmt.setString(5, car.getRegistrationNumber());
            stmt.setString(6, car.getVinCode());
            stmt.setString(7, car.getTransmissionType().name());
            stmt.setString(8, car.getFuelType().name());
            stmt.setInt(9, car.getSeatsCount());
            stmt.setBigDecimal(10, car.getDailyRate());
            stmt.setString(11, car.getStatus().name());
            stmt.setInt(12, car.getMileage());
            stmt.setString(13, car.getImageUrl());
            stmt.setString(14, car.getDescription());
            stmt.setLong(15, car.getCarId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return car;
    }

    @Override
    public boolean deleteById(Long carId) {
        String sql = "DELETE FROM cars WHERE car_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, carId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Car> findAll() {
        String sql = "SELECT * FROM cars ORDER BY brand, model";
        List<Car> cars = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                cars.add(mapResultSetToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    @Override
    public List<Car> findByStatus(CarStatus status) {
        String sql = "SELECT * FROM cars WHERE status = ? ORDER BY brand, model";
        List<Car> cars = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cars.add(mapResultSetToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    @Override
    public List<Car> findAvailableCars() {
        return findByStatus(CarStatus.AVAILABLE);
    }

    @Override
    public List<Car> findByBrand(String brand) {
        String sql = "SELECT * FROM cars WHERE brand = ? ORDER BY model, year DESC";
        List<Car> cars = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, brand);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cars.add(mapResultSetToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    @Override
    public List<Car> findByTransmissionType(TransmissionType transmissionType) {
        String sql = "SELECT * FROM cars WHERE transmission_type = ? ORDER BY brand, model";
        List<Car> cars = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, transmissionType.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cars.add(mapResultSetToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    @Override
    public List<Car> findByFuelType(FuelType fuelType) {
        String sql = "SELECT * FROM cars WHERE fuel_type = ? ORDER BY brand, model";
        List<Car> cars = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, fuelType.name());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cars.add(mapResultSetToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    @Override
    public List<Car> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        String sql = "SELECT * FROM cars WHERE daily_rate BETWEEN ? AND ? ORDER BY daily_rate";
        List<Car> cars = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBigDecimal(1, minPrice);
            stmt.setBigDecimal(2, maxPrice);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cars.add(mapResultSetToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    @Override
    public List<Car> searchCars(String brand, CarStatus status, TransmissionType transmission,
                                FuelType fuel, BigDecimal maxPrice) {
        StringBuilder sql = new StringBuilder("SELECT * FROM cars WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (brand != null && !brand.isEmpty()) {
            sql.append(" AND brand = ?");
            params.add(brand);
        }
        if (status != null) {
            sql.append(" AND status = ?");
            params.add(status.name());
        }
        if (transmission != null) {
            sql.append(" AND transmission_type = ?");
            params.add(transmission.name());
        }
        if (fuel != null) {
            sql.append(" AND fuel_type = ?");
            params.add(fuel.name());
        }
        if (maxPrice != null) {
            sql.append(" AND daily_rate <= ?");
            params.add(maxPrice);
        }
        sql.append(" ORDER BY brand, model");

        List<Car> cars = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                cars.add(mapResultSetToCar(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cars;
    }

    @Override
    public boolean updateStatus(Long carId, CarStatus newStatus) {
        String sql = "UPDATE cars SET status = ? WHERE car_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, newStatus.name());
            stmt.setLong(2, carId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateMileage(Long carId, Integer newMileage) {
        String sql = "UPDATE cars SET mileage = ? WHERE car_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, newMileage);
            stmt.setLong(2, carId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean existsByRegistrationNumber(String registrationNumber) {
        String sql = "SELECT COUNT(*) FROM cars WHERE registration_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, registrationNumber);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public long countAll() {
        String sql = "SELECT COUNT(*) FROM cars";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long countAvailable() {
        String sql = "SELECT COUNT(*) FROM cars WHERE status = 'AVAILABLE'";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Car mapResultSetToCar(ResultSet rs) throws SQLException {
        Car car = new Car();
        car.setCarId(rs.getLong("car_id"));
        car.setBrand(rs.getString("brand"));
        car.setModel(rs.getString("model"));
        car.setYear(rs.getInt("year"));
        car.setColor(rs.getString("color"));
        car.setRegistrationNumber(rs.getString("registration_number"));
        car.setVinCode(rs.getString("vin_code"));
        car.setTransmissionType(TransmissionType.valueOf(rs.getString("transmission_type")));
        car.setFuelType(FuelType.valueOf(rs.getString("fuel_type")));
        car.setSeatsCount(rs.getInt("seats_count"));
        car.setDailyRate(rs.getBigDecimal("daily_rate"));
        car.setStatus(CarStatus.valueOf(rs.getString("status")));
        car.setMileage(rs.getInt("mileage"));
        car.setImageUrl(rs.getString("image_url"));
        car.setDescription(rs.getString("description"));

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            car.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
        if (updatedTimestamp != null) {
            car.setUpdatedAt(updatedTimestamp.toLocalDateTime());
        }

        return car;
    }
}