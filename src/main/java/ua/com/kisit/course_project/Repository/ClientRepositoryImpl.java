package ua.com.kisit.course_project.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.com.kisit.course_project.Entity.Client;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository  // FIXED: додана анотація + замінено Connection → JdbcTemplate
public class ClientRepositoryImpl implements ClientRepository {

    private final JdbcTemplate jdbcTemplate;

    public ClientRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Client> clientRowMapper = (rs, rowNum) -> {
        Client client = new Client();
        client.setClientId(rs.getLong("client_id"));
        client.setUserId(rs.getLong("user_id"));
        client.setFirstName(rs.getString("first_name"));
        client.setLastName(rs.getString("last_name"));
        client.setPassportSeries(rs.getString("passport_series"));
        client.setPassportNumber(rs.getString("passport_number"));
        client.setPassportIssuedBy(rs.getString("passport_issued_by"));
        Date passportIssueDate = rs.getDate("passport_issue_date");
        if (passportIssueDate != null) client.setPassportIssueDate(passportIssueDate.toLocalDate());
        client.setPhone(rs.getString("phone"));
        client.setAddress(rs.getString("address"));
        Date dateOfBirth = rs.getDate("date_of_birth");
        if (dateOfBirth != null) client.setDateOfBirth(dateOfBirth.toLocalDate());
        client.setDriverLicenseNumber(rs.getString("driver_license_number"));
        Date licenseDate = rs.getDate("driver_license_issue_date");
        if (licenseDate != null) client.setDriverLicenseIssueDate(licenseDate.toLocalDate());
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) client.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) client.setUpdatedAt(updatedAt.toLocalDateTime());
        return client;
    };

    @Override
    public Optional<Client> findById(Long clientId) {
        List<Client> result = jdbcTemplate.query(
                "SELECT * FROM clients WHERE client_id=?", clientRowMapper, clientId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Client> findByUserId(Long userId) {
        List<Client> result = jdbcTemplate.query(
                "SELECT * FROM clients WHERE user_id=?", clientRowMapper, userId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Client> findByPassport(String passportSeries, String passportNumber) {
        List<Client> result = jdbcTemplate.query(
                "SELECT * FROM clients WHERE passport_series=? AND passport_number=?",
                clientRowMapper, passportSeries, passportNumber);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Client> findByPhone(String phone) {
        List<Client> result = jdbcTemplate.query(
                "SELECT * FROM clients WHERE phone=?", clientRowMapper, phone);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Client> findByDriverLicense(String licenseNumber) {
        List<Client> result = jdbcTemplate.query(
                "SELECT * FROM clients WHERE driver_license_number=?", clientRowMapper, licenseNumber);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Client save(Client client) {
        String sql = "INSERT INTO clients (user_id, first_name, last_name, passport_series, " +
                "passport_number, passport_issued_by, passport_issue_date, phone, address, " +
                "date_of_birth, driver_license_number, driver_license_issue_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, client.getUserId());
            ps.setString(2, client.getFirstName());
            ps.setString(3, client.getLastName());
            ps.setString(4, client.getPassportSeries());
            ps.setString(5, client.getPassportNumber());
            ps.setString(6, client.getPassportIssuedBy());
            ps.setDate(7, client.getPassportIssueDate() != null ? Date.valueOf(client.getPassportIssueDate()) : null);
            ps.setString(8, client.getPhone());
            ps.setString(9, client.getAddress());
            ps.setDate(10, client.getDateOfBirth() != null ? Date.valueOf(client.getDateOfBirth()) : null);
            ps.setString(11, client.getDriverLicenseNumber());
            ps.setDate(12, client.getDriverLicenseIssueDate() != null ? Date.valueOf(client.getDriverLicenseIssueDate()) : null);
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            client.setClientId(keyHolder.getKey().longValue());
        }
        return client;
    }

    @Override
    public Client update(Client client) {
        String sql = "UPDATE clients SET first_name=?, last_name=?, passport_series=?, " +
                "passport_number=?, passport_issued_by=?, passport_issue_date=?, phone=?, " +
                "address=?, date_of_birth=?, driver_license_number=?, driver_license_issue_date=? " +
                "WHERE client_id=?";

        jdbcTemplate.update(sql,
                client.getFirstName(), client.getLastName(),
                client.getPassportSeries(), client.getPassportNumber(), client.getPassportIssuedBy(),
                client.getPassportIssueDate() != null ? Date.valueOf(client.getPassportIssueDate()) : null,
                client.getPhone(), client.getAddress(),
                client.getDateOfBirth() != null ? Date.valueOf(client.getDateOfBirth()) : null,
                client.getDriverLicenseNumber(),
                client.getDriverLicenseIssueDate() != null ? Date.valueOf(client.getDriverLicenseIssueDate()) : null,
                client.getClientId());
        return client;
    }

    @Override
    public boolean deleteById(Long clientId) {
        return jdbcTemplate.update("DELETE FROM clients WHERE client_id=?", clientId) > 0;
    }

    @Override
    public List<Client> findAll() {
        return jdbcTemplate.query("SELECT * FROM clients ORDER BY created_at DESC", clientRowMapper);
    }

    @Override
    public List<Client> searchByName(String searchTerm) {
        String pattern = "%" + searchTerm + "%";
        return jdbcTemplate.query(
                "SELECT * FROM clients WHERE first_name LIKE ? OR last_name LIKE ? ORDER BY last_name, first_name",
                clientRowMapper, pattern, pattern);
    }

    @Override
    public boolean existsByPassport(String passportSeries, String passportNumber) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE passport_series=? AND passport_number=?",
                Integer.class, passportSeries, passportNumber);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByPhone(String phone) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE phone=?", Integer.class, phone);
        return count != null && count > 0;
    }

    @Override
    public boolean existsByDriverLicense(String licenseNumber) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM clients WHERE driver_license_number=?", Integer.class, licenseNumber);
        return count != null && count > 0;
    }
}