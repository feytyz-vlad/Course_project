package ua.com.kisit.course_project.Repository;

import ua.com.kisit.course_project.Entity.Client;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Client entity
 */
public interface ClientRepository {

    /**
     * Find client by ID
     */
    Optional<Client> findById(Long clientId);

    /**
     * Find client by user ID
     */
    Optional<Client> findByUserId(Long userId);

    /**
     * Find client by passport
     */
    Optional<Client> findByPassport(String passportSeries, String passportNumber);

    /**
     * Find client by phone
     */
    Optional<Client> findByPhone(String phone);

    /**
     * Find client by driver license
     */
    Optional<Client> findByDriverLicense(String licenseNumber);

    /**
     * Save new client
     */
    Client save(Client client);

    /**
     * Update existing client
     */
    Client update(Client client);

    /**
     * Delete client by ID
     */
    boolean deleteById(Long clientId);

    /**
     * Find all clients
     */
    List<Client> findAll();

    /**
     * Search clients by name
     */
    List<Client> searchByName(String searchTerm);

    /**
     * Check if passport exists
     */
    boolean existsByPassport(String passportSeries, String passportNumber);

    /**
     * Check if phone exists
     */
    boolean existsByPhone(String phone);

    /**
     * Check if driver license exists
     */
    boolean existsByDriverLicense(String licenseNumber);
}