package ua.com.kisit.course_project.Service;

import org.springframework.stereotype.Service;
import ua.com.kisit.course_project.Entity.Client;
import ua.com.kisit.course_project.Repository.ClientRepository;

import java.util.List;
import java.util.Optional;

@Service  // FIXED: додана анотація — без неї Spring не бачить цей клас як bean
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client registerClient(Client client) {
        if (clientRepository.existsByPassport(client.getPassportSeries(), client.getPassportNumber())) {
            throw new IllegalArgumentException("Клієнт з таким паспортом вже зареєстрований");
        }
        if (clientRepository.existsByPhone(client.getPhone())) {
            throw new IllegalArgumentException("Клієнт з таким телефоном вже зареєстрований");
        }
        if (clientRepository.existsByDriverLicense(client.getDriverLicenseNumber())) {
            throw new IllegalArgumentException("Клієнт з таким водійським посвідченням вже зареєстрований");
        }
        return clientRepository.save(client);
    }

    /**
     * FIXED: Доданий метод для створення профілю після реєстрації.
     * Викликається з WebHomeController після заповнення форми профілю.
     */
    public Client createClientProfile(Client client) {
        // Перевірка чи профіль вже існує
        if (clientRepository.findByUserId(client.getUserId()).isPresent()) {
            throw new IllegalStateException("Профіль для цього користувача вже існує");
        }

        if (clientRepository.existsByPhone(client.getPhone())) {
            throw new IllegalArgumentException("Клієнт з таким телефоном вже зареєстрований");
        }

        if (client.getDriverLicenseNumber() != null && !client.getDriverLicenseNumber().isBlank()
                && clientRepository.existsByDriverLicense(client.getDriverLicenseNumber())) {
            throw new IllegalArgumentException("Клієнт з таким водійським посвідченням вже зареєстрований");
        }

        // Prevent NOT NULL constraint violations for missing fields
        if (client.getPassportSeries() == null) client.setPassportSeries("");
        if (client.getPassportNumber() == null) client.setPassportNumber("");
        if (client.getPassportIssuedBy() == null) client.setPassportIssuedBy("");
        if (client.getAddress() == null) client.setAddress("");

        return clientRepository.save(client);
    }

    public Client updateClient(Client client) {
        Optional<Client> existing = clientRepository.findById(client.getClientId());
        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Клієнта не знайдено");
        }
        return clientRepository.update(client);
    }

    public boolean deleteClient(Long clientId) {
        return clientRepository.deleteById(clientId);
    }

    public Optional<Client> getClientById(Long clientId) {
        return clientRepository.findById(clientId);
    }

    public Optional<Client> getClientByUserId(Long userId) {
        return clientRepository.findByUserId(userId);
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public List<Client> searchClientsByName(String searchTerm) {
        return clientRepository.searchByName(searchTerm);
    }
}