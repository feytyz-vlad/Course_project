package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.UserSession;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserSession entity
 */
public interface UserSessionRepository {

    /**
     * Save new session
     * @param session session to save
     * @return saved session with generated ID
     */
    UserSession save(UserSession session);

    /**
     * Find session by token
     * @param token session token
     * @return Optional containing session if found
     */
    Optional<UserSession> findByToken(String token);

    /**
     * Find active sessions for user
     * @param userId user ID
     * @return list of active sessions
     */
    List<UserSession> findActiveSessionsByUserId(Long userId);

    /**
     * Invalidate session
     * @param token session token
     * @return true if invalidated successfully
     */
    boolean invalidateSession(String token);

    /**
     * Invalidate all user sessions
     * @param userId user ID
     * @return true if invalidated successfully
     */
    boolean invalidateAllUserSessions(Long userId);

    /**
     * Delete expired sessions
     * @return number of deleted sessions
     */
    int deleteExpiredSessions();
}