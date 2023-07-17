package com.example.authservice;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.exception.EntityNotFoundException;
import com.example.authservice.model.Client;
import com.example.authservice.repository.ClientRepository;
import com.example.authservice.service.ClientService;
import com.example.authservice.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthServiceApplicationTests {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ClientService clientService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientServiceImpl(clientRepository, passwordEncoder);
    }

    @Test
    public void testAddClient_Success() {
        String username = "adilet";
        String password = "Adilet.123";
        AuthRequest authRequest = new AuthRequest(username, password);
        when(clientRepository.existsByUsername(username)).thenReturn(false);

        Client savedClient = new Client();
        savedClient.setUsername(username);
        savedClient.setPassword("encodedPassword");

        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        Client createdClient = clientService.addClient(authRequest);

        assertNotNull(createdClient);
        assertEquals(username, createdClient.getUsername());
        assertEquals("encodedPassword", createdClient.getPassword());
        verify(clientRepository, times(1)).existsByUsername(username);
        verify(passwordEncoder, times(1)).encode(password);
        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    public void testFindByUserName_Success() {
        String username = "adilet";
        Client client = new Client();
        client.setUsername(username);

        when(clientRepository.findByUsername(username)).thenReturn(Optional.of(client));

        Client result = clientService.findByUserName(username);

        assertNotNull(result);
        assertEquals(username, result.getUsername());

        verify(clientRepository, times(1)).findByUsername(username);
    }

    @Test
    public void testFindByUserName_NotFound() {
        String username = "adilet";

        when(clientRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            clientService.findByUserName(username);
        });

        verify(clientRepository, times(1)).findByUsername(username);
    }

}
