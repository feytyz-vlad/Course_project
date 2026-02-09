package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ClientRepository using JDBC
 */
public class ClientRepositoryImpl implements ClientRepository {

    private final Connection connection;

    public ClientRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Client> findById(Long clientId) {
        String sql = "SELECT * FROM clients WHERE client_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, clientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<Client> findByUserId(Long userId) {
        String sql = "SELECT * FROM clients WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<Client> findByPassport(String passportSeries, String passportNumber) {
        String sql = "SELECT * FROM clients WHERE passport_series = ? AND passport_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, passportSeries);
            stmt.setString(2, passportNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<Client> findByPhone(String phone) {
        String sql = "SELECT * FROM clients WHERE phone = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, phone);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Optional<Client> findByDriverLicense(String licenseNumber) {
        String sql = "SELECT * FROM clients WHERE driver_license_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, licenseNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public Client save(Client client) {
        String sql = "INSERT INTO clients (user_id, first_name, last_name, passport_series, " +
                "passport_number, passport_issued_by, passport_issue_date, phone, address, " +
                "date_of_birth, driver_license_number, driver_license_issue_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, client.getUserId());
            stmt.setString(2, client.getFirstName());
            stmt.setString(3, client.getLastName());
            stmt.setString(4, client.getPassportSeries());
            stmt.setString(5, client.getPassportNumber());
            stmt.setString(6, client.getPassportIssuedBy());
            stmt.setDate(7, client.getPassportIssueDate() != null ?
                    Date.valueOf(client.getPassportIssueDate()) : null);
            stmt.setString(8, client.getPhone());
            stmt.setString(9, client.getAddress());
            stmt.setDate(10, client.getDateOfBirth() != null ?
                    Date.valueOf(client.getDateOfBirth()) : null);
            stmt.setString(11, client.getDriverLicenseNumber());
            stmt.setDate(12, client.getDriverLicenseIssueDate() != null ?
                    Date.valueOf(client.getDriverLicenseIssueDate()) : null);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    client.setClientId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return client;
    }

    @Override
    public Client update(Client client) {
        String sql = "UPDATE clients SET first_name = ?, last_name = ?, passport_series = ?, " +
                "passport_number = ?, passport_issued_by = ?, passport_issue_date = ?, " +
                "phone = ?, address = ?, date_of_birth = ?, driver_license_number = ?, " +
                "driver_license_issue_date = ? WHERE client_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, client.getFirstName());
            stmt.setString(2, client.getLastName());
            stmt.setString(3, client.getPassportSeries());
            stmt.setString(4, client.getPassportNumber());
            stmt.setString(5, client.getPassportIssuedBy());
            stmt.setDate(6, client.getPassportIssueDate() != null ?
                    Date.valueOf(client.getPassportIssueDate()) : null);
            stmt.setString(7, client.getPhone());
            stmt.setString(8, client.getAddress());
            stmt.setDate(9, client.getDateOfBirth() != null ?
                    Date.valueOf(client.getDateOfBirth()) : null);
            stmt.setString(10, client.getDriverLicenseNumber());
            stmt.setDate(11, client.getDriverLicenseIssueDate() != null ?
                    Date.valueOf(client.getDriverLicenseIssueDate()) : null);
            stmt.setLong(12, client.getClientId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return client;
    }

    @Override
    public boolean deleteById(Long clientId) {
        String sql = "DELETE FROM clients WHERE client_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, clientId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<Client> findAll() {
        String sql = "SELECT * FROM clients ORDER BY created_at DESC";
        List<Client> clients = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clients;
    }

    @Override
    public List<Client> searchByName(String searchTerm) {
        String sql = "SELECT * FROM clients WHERE first_name LIKE ? OR last_name LIKE ? " +
                "ORDER BY last_name, first_name";
        List<Client> clients = new ArrayList<>();
        String pattern = "%" + searchTerm + "%";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                clients.add(mapResultSetToClient(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clients;
    }

    @Override
    public boolean existsByPassport(String passportSeries, String passportNumber) {
        String sql = "SELECT COUNT(*) FROM clients WHERE passport_series = ? AND passport_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, passportSeries);
            stmt.setString(2, passportNumber);
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
    public boolean existsByPhone(String phone) {
        String sql = "SELECT COUNT(*) FROM clients WHERE phone = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, phone);
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
    public boolean existsByDriverLicense(String licenseNumber) {
        String sql = "SELECT COUNT(*) FROM clients WHERE driver_license_number = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, licenseNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Helper method to map ResultSet to Client object
     */
    private Client mapResultSetToClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setClientId(rs.getLong("client_id"));
        client.setUserId(rs.getLong("user_id"));
        client.setFirstName(rs.getString("first_name"));
        client.setLastName(rs.getString("last_name"));
        client.setPassportSeries(rs.getString("passport_series"));
        client.setPassportNumber(rs.getString("passport_number"));
        client.setPassportIssuedBy(rs.getString("passport_issued_by"));

        Date passportIssueDate = rs.getDate("passport_issue_date");
        if (passportIssueDate != null) {
            client.setPassportIssueDate(passportIssueDate.toLocalDate());
        }

        client.setPhone(rs.getString("phone"));
        client.setAddress(rs.getString("address"));

        Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) {
            client.setDateOfBirth(dateOfBirth.toLocalDate());
        }

        client.setDriverLicenseNumber(rs.getString("driver_license_number"));

        Date licenseIssueDate = rs.getDate("driver_license_issue_date");
        if (licenseIssueDate != null) {
            client.setDriverLicenseIssueDate(licenseIssueDate.toLocalDate());
        }

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            client.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp updatedTimestamp = rs.getTimestamp("updated_at");
        if (updatedTimestamp != null) {
            client.setUpdatedAt(updatedTimestamp.toLocalDateTime());
        }

        return client;
    }
}