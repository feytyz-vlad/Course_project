package ua.com.kisit.course_project.Controller;

import ua.com.kisit.course_project.Entity.Client;
import ua.com.kisit.course_project.Service.ClientService;

import java.util.List;
import java.util.Optional;

public class ClientController {
    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    public Client registerClient(Client client) {
        try {
            return clientService.registerClient(client);
        } catch (IllegalArgumentException e) {
            System.err.println("Error registering client: " + e.getMessage());
            return null;
        }
    }

    public Client updateClient(Client client) {
        try {
            return clientService.updateClient(client);
        } catch (IllegalArgumentException e) {
            System.err.println("Error updating client: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteClient(Long clientId) {
        return clientService.deleteClient(clientId);
    }

    public Optional<Client> getClientById(Long clientId) {
        return clientService.getClientById(clientId);
    }

    public Optional<Client> getClientByUserId(Long userId) {
        return clientService.getClientByUserId(userId);
    }

    public List<Client> getAllClients() {
        return clientService.getAllClients();
    }

    public List<Client> searchClients(String searchTerm) {
        return clientService.searchClientsByName(searchTerm);
    }

    public void displayClientInfo(Client client) {
        System.out.println("\n=== Інформація про клієнта ===");
        System.out.println("ПІБ: " + client.getFullName());
        System.out.println("Телефон: " + client.getPhone());
        System.out.println("Паспорт: " + client.getPassportFullNumber());
        System.out.println("Водійське посвідчення: " + client.getDriverLicenseNumber());
        System.out.println("Адреса: " + client.getAddress());
    }
}