package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.UserSession;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserSessionRepository using JDBC
 */
public class UserSessionRepositoryImpl implements UserSessionRepository {

    private final Connection connection;

    public UserSessionRepositoryImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public UserSession save(UserSession session) {
        String sql = "INSERT INTO user_sessions (user_id, session_token, ip_address, user_agent, expires_at, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, session.getUserId());
            stmt.setString(2, session.getSessionToken());
            stmt.setString(3, session.getIpAddress());
            stmt.setString(4, session.getUserAgent());
            stmt.setTimestamp(5, Timestamp.valueOf(session.getExpiresAt()));
            stmt.setBoolean(6, session.isActive());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    session.setSessionId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        return session;
    }

    @Override
    public Optional<UserSession> findByToken(String token) {
        String sql = "SELECT * FROM user_sessions WHERE session_token = ? AND is_active = true";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Optional.empty();
    }

    @Override
    public List<UserSession> findActiveSessionsByUserId(Long userId) {
        String sql = "SELECT * FROM user_sessions WHERE user_id = ? AND is_active = true " +
                "AND expires_at > NOW() ORDER BY created_at DESC";
        List<UserSession> sessions = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sessions;
    }

    @Override
    public boolean invalidateSession(String token) {
        String sql = "UPDATE user_sessions SET is_active = false WHERE session_token = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, token);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean invalidateAllUserSessions(Long userId) {
        String sql = "UPDATE user_sessions SET is_active = false WHERE user_id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int deleteExpiredSessions() {
        String sql = "DELETE FROM user_sessions WHERE expires_at < NOW()";

        try (Statement stmt = connection.createStatement()) {
            return stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * Helper method to map ResultSet to UserSession object
     */
    private UserSession mapResultSetToSession(ResultSet rs) throws SQLException {
        UserSession session = new UserSession();
        session.setSessionId(rs.getLong("session_id"));
        session.setUserId(rs.getLong("user_id"));
        session.setSessionToken(rs.getString("session_token"));
        session.setIpAddress(rs.getString("ip_address"));
        session.setUserAgent(rs.getString("user_agent"));
        session.setActive(rs.getBoolean("is_active"));

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            session.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp expiresTimestamp = rs.getTimestamp("expires_at");
        if (expiresTimestamp != null) {
            session.setExpiresAt(expiresTimestamp.toLocalDateTime());
        }

        return session;
    }
}