package ua.com.kisit.course_project.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository  // FIXED: додана анотація + замінено Connection → JdbcTemplate
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setUserId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setActive(rs.getBoolean("is_active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) user.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) user.setUpdatedAt(updatedAt.toLocalDateTime());
        return user;
    };

    @Override
    public Optional<User> findByEmail(String email) {
        List<User> result = jdbcTemplate.query(
                "SELECT * FROM users WHERE email=?", userRowMapper, email);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<User> findById(Long userId) {
        List<User> result = jdbcTemplate.query(
                "SELECT * FROM users WHERE user_id=?", userRowMapper, userId);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public User save(User user) {
        String sql = "INSERT INTO users (email, password_hash, role, is_active) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRole().name());
            ps.setBoolean(4, user.isActive());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            user.setUserId(keyHolder.getKey().longValue());
        }
        return user;
    }

    @Override
    public User update(User user) {
        jdbcTemplate.update(
                "UPDATE users SET email=?, password_hash=?, role=?, is_active=? WHERE user_id=?",
                user.getEmail(), user.getPasswordHash(), user.getRole().name(),
                user.isActive(), user.getUserId());
        return user;
    }

    @Override
    public boolean deleteById(Long userId) {
        return jdbcTemplate.update("DELETE FROM users WHERE user_id=?", userId) > 0;
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users ORDER BY created_at DESC", userRowMapper);
    }

    @Override
    public List<User> findByRole(UserRole role) {
        return jdbcTemplate.query("SELECT * FROM users WHERE role=? ORDER BY created_at DESC",
                userRowMapper, role.name());
    }

    @Override
    public List<User> findActiveUsers() {
        return jdbcTemplate.query("SELECT * FROM users WHERE is_active=true ORDER BY created_at DESC",
                userRowMapper);
    }

    @Override
    public boolean existsByEmail(String email) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email=?", Integer.class, email);
        return count != null && count > 0;
    }

    @Override
    public boolean updatePassword(Long userId, String newPasswordHash) {
        return jdbcTemplate.update(
                "UPDATE users SET password_hash=? WHERE user_id=?", newPasswordHash, userId) > 0;
    }

    @Override
    public boolean setActiveStatus(Long userId, boolean isActive) {
        return jdbcTemplate.update(
                "UPDATE users SET is_active=? WHERE user_id=?", isActive, userId) > 0;
    }
}