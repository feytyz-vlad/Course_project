package ua.com.kisit.course_project.Repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ua.com.kisit.course_project.Entity.UserSession;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository  // FIXED: додана анотація + замінено Connection → JdbcTemplate
public class UserSessionRepositoryImpl implements UserSessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserSessionRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<UserSession> sessionRowMapper = (rs, rowNum) -> {
        UserSession session = new UserSession();
        session.setSessionId(rs.getLong("session_id"));
        session.setUserId(rs.getLong("user_id"));
        session.setSessionToken(rs.getString("session_token"));
        session.setIpAddress(rs.getString("ip_address"));
        session.setUserAgent(rs.getString("user_agent"));
        session.setActive(rs.getBoolean("is_active"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) session.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp expiresAt = rs.getTimestamp("expires_at");
        if (expiresAt != null) session.setExpiresAt(expiresAt.toLocalDateTime());
        return session;
    };

    @Override
    public UserSession save(UserSession session) {
        String sql = "INSERT INTO user_sessions (user_id, session_token, ip_address, user_agent, expires_at, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, session.getUserId());
            ps.setString(2, session.getSessionToken());
            ps.setString(3, session.getIpAddress());
            ps.setString(4, session.getUserAgent());
            ps.setTimestamp(5, Timestamp.valueOf(session.getExpiresAt()));
            ps.setBoolean(6, session.isActive());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            session.setSessionId(keyHolder.getKey().longValue());
        }
        return session;
    }

    @Override
    public Optional<UserSession> findByToken(String token) {
        List<UserSession> result = jdbcTemplate.query(
                "SELECT * FROM user_sessions WHERE session_token=? AND is_active=true",
                sessionRowMapper, token);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public List<UserSession> findActiveSessionsByUserId(Long userId) {
        return jdbcTemplate.query(
                "SELECT * FROM user_sessions WHERE user_id=? AND is_active=true AND expires_at > NOW() ORDER BY created_at DESC",
                sessionRowMapper, userId);
    }

    @Override
    public boolean invalidateSession(String token) {
        return jdbcTemplate.update(
                "UPDATE user_sessions SET is_active=false WHERE session_token=?", token) > 0;
    }

    @Override
    public boolean invalidateAllUserSessions(Long userId) {
        return jdbcTemplate.update(
                "UPDATE user_sessions SET is_active=false WHERE user_id=?", userId) > 0;
    }

    @Override
    public int deleteExpiredSessions() {
        return jdbcTemplate.update("DELETE FROM user_sessions WHERE expires_at < NOW()");
    }
}