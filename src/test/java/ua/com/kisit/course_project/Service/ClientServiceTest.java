package ua.com.kisit.course_project.Service;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.com.kisit.course_project.Entity.Client;
import ua.com.kisit.course_project.Repository.ClientRepository;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Test
    void registerClient_Success() {
        Client client = new Client(1L, "John", "Doe", "AB", "123456", "+380501111111", "LIC123");
        
        when(clientRepository.existsByPassport("AB", "123456")).thenReturn(false);
        when(clientRepository.existsByPhone("+380501111111")).thenReturn(false);
        when(clientRepository.existsByDriverLicense("LIC123")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client result = clientService.registerClient(client);

        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void registerClient_ThrowsException_WhenPassportExists() {
        Client client = new Client(1L, "John", "Doe", "AB", "123456", "+380501111111", "LIC123");
        
        when(clientRepository.existsByPassport("AB", "123456")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.registerClient(client);
        });

        assertEquals("Клієнт з таким паспортом вже зареєстрований", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void registerClient_ThrowsException_WhenPhoneExists() {
        Client client = new Client(1L, "John", "Doe", "AB", "123456", "+380501111111", "LIC123");
        
        when(clientRepository.existsByPassport("AB", "123456")).thenReturn(false);
        when(clientRepository.existsByPhone("+380501111111")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.registerClient(client);
        });

        assertEquals("Клієнт з таким телефоном вже зареєстрований", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void registerClient_ThrowsException_WhenDriverLicenseExists() {
        Client client = new Client(1L, "John", "Doe", "AB", "123456", "+380501111111", "LIC123");
        
        when(clientRepository.existsByPassport("AB", "123456")).thenReturn(false);
        when(clientRepository.existsByPhone("+380501111111")).thenReturn(false);
        when(clientRepository.existsByDriverLicense("LIC123")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.registerClient(client);
        });

        assertEquals("Клієнт з таким водійським посвідченням вже зареєстрований", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void createClientProfile_Success() {
        Client client = new Client();
        client.setUserId(10L);
        client.setPhone("+380501111111");
        client.setDriverLicenseNumber("LIC123");

        when(clientRepository.findByUserId(10L)).thenReturn(Optional.empty());
        when(clientRepository.existsByPhone("+380501111111")).thenReturn(false);
        when(clientRepository.existsByDriverLicense("LIC123")).thenReturn(false);
        when(clientRepository.save(any(Client.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Client result = clientService.createClientProfile(client);

        assertNotNull(result);
        assertEquals("", result.getPassportSeries());
        assertEquals("", result.getPassportNumber());
        assertEquals("", result.getPassportIssuedBy());
        assertEquals("", result.getAddress());
        verify(clientRepository, times(1)).save(client);
    }

    @Test
    void createClientProfile_ThrowsException_WhenProfileExists() {
        Client client = new Client();
        client.setUserId(10L);

        when(clientRepository.findByUserId(10L)).thenReturn(Optional.of(new Client()));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            clientService.createClientProfile(client);
        });

        assertEquals("Профіль для цього користувача вже існує", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateClient_Success() {
        Client client = new Client();
        client.setClientId(5L);

        when(clientRepository.findById(5L)).thenReturn(Optional.of(client));
        when(clientRepository.update(any(Client.class))).thenReturn(client);

        Client result = clientService.updateClient(client);

        assertNotNull(result);
        verify(clientRepository, times(1)).update(client);
    }

    @Test
    void updateClient_ThrowsException_WhenNotFound() {
        Client client = new Client();
        client.setClientId(5L);

        when(clientRepository.findById(5L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.updateClient(client);
        });

        assertEquals("Клієнта не знайдено", exception.getMessage());
        verify(clientRepository, never()).update(any(Client.class));
    }

    @Test
    void deleteClient_Success() {
        when(clientRepository.deleteById(5L)).thenReturn(true);
        boolean result = clientService.deleteClient(5L);
        assertTrue(result);
        verify(clientRepository, times(1)).deleteById(5L);
    }

    @Test
    void getClientById_Success() {
        Client client = new Client();
        client.setClientId(5L);
        when(clientRepository.findById(5L)).thenReturn(Optional.of(client));

        Optional<Client> result = clientService.getClientById(5L);

        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getClientId());
    }

    @Test
    void getClientByUserId_Success() {
        Client client = new Client();
        client.setUserId(10L);
        when(clientRepository.findByUserId(10L)).thenReturn(Optional.of(client));

        Optional<Client> result = clientService.getClientByUserId(10L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getUserId());
    }

    @Test
    void getAllClients_Success() {
        List<Client> clients = List.of(new Client(), new Client());
        when(clientRepository.findAll()).thenReturn(clients);

        List<Client> result = clientService.getAllClients();

        assertEquals(2, result.size());
    }

    @Test
    void searchClientsByName_Success() {
        List<Client> clients = List.of(new Client());
        when(clientRepository.searchByName("John")).thenReturn(clients);

        List<Client> result = clientService.searchClientsByName("John");

        assertEquals(1, result.size());
    }
}
