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
    public Client createClientProfile(Long userId, String firstName, String lastName,
                                      String phone, String driverLicense, String rnokpp) {
        // Перевірка чи профіль вже існує
        if (clientRepository.findByUserId(userId).isPresent()) {
            throw new IllegalStateException("Профіль для цього користувача вже існує");
        }

        if (clientRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Клієнт з таким телефоном вже зареєстрований");
        }

        if (driverLicense != null && !driverLicense.isBlank()
                && clientRepository.existsByDriverLicense(driverLicense)) {
            throw new IllegalArgumentException("Клієнт з таким водійським посвідченням вже зареєстрований");
        }

        Client client = new Client();
        client.setUserId(userId);
        client.setFirstName(firstName);
        client.setLastName(lastName);
        client.setPhone(phone);
        client.setDriverLicenseNumber(driverLicense);
        client.setRnokpp(rnokpp);
        
        // Prevent NOT NULL constraint violations for fields we no longer collect
        client.setPassportSeries("");
        client.setPassportNumber("");
        client.setPassportIssuedBy("");
        client.setAddress("");

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