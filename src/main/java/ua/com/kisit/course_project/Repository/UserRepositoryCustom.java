package ua.com.kisit.course_project.Repository;

import java.util.List;
import java.util.Optional;

import ua.com.kisit.course_project.Entity.User;

public interface UserRepositoryCustom {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long userId);
    User save(User user);
    boolean deleteById(Long userId);
    List<User> findAll();
    boolean existsByEmail(String email);
    int updatePassword(Long userId, String newPasswordHash);
}
