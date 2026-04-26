package ua.com.kisit.course_project.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ua.com.kisit.course_project.Entity.User;

/**
 * Repository interface for User entity
 * Defines database operations for user management
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email address
     * @param email user's email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if email already exists
     * @param email email to check
     * @return true if exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Update user password
     * @param userId user ID
     * @param newPasswordHash new password hash
     * @return true if updated successfully
     */
    @Modifying
    @Query("UPDATE User u SET u.passwordHash = :passwordHash WHERE u.userId = :userId")
    int updatePassword(@Param("userId") Long userId, @Param("passwordHash") String passwordHash);
}