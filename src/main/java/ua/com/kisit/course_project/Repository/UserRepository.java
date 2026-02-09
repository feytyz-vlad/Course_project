package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.User;
import ua.com.kisit.course_project.Entity.UserRole;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * Defines database operations for user management
 */
public interface UserRepository {

    /**
     * Find user by email address
     * @param email user's email
     * @return Optional containing user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by ID
     * @param userId user's ID
     * @return Optional containing user if found
     */
    Optional<User> findById(Long userId);

    /**
     * Save new user to database
     * @param user user to save
     * @return saved user with generated ID
     */
    User save(User user);

    /**
     * Update existing user
     * @param user user to update
     * @return updated user
     */
    User update(User user);

    /**
     * Delete user by ID
     * @param userId ID of user to delete
     * @return true if deleted successfully
     */
    boolean deleteById(Long userId);

    /**
     * Find all users
     * @return list of all users
     */
    List<User> findAll();

    /**
     * Find users by role
     * @param role user role
     * @return list of users with specified role
     */
    List<User> findByRole(UserRole role);

    /**
     * Find active users
     * @return list of active users
     */
    List<User> findActiveUsers();

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
    boolean updatePassword(Long userId, String newPasswordHash);

    /**
     * Activate or deactivate user account
     * @param userId user ID
     * @param isActive activation status
     * @return true if updated successfully
     */
    boolean setActiveStatus(Long userId, boolean isActive);
}