package com.example.authservice.service.impl;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.exception.EntityNotFoundException;
import com.example.authservice.exception.ExistingEntityCreationException;
import com.example.authservice.model.Client;
import com.example.authservice.repository.ClientRepository;
import com.example.authservice.service.ClientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder;

    public ClientServiceImpl(ClientRepository clientRepository, PasswordEncoder passwordEncoder) {
        this.clientRepository = clientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Client addClient(AuthRequest authRequest) {
        if (clientRepository.existsByUsername(authRequest.getUsername())) {
            throw new ExistingEntityCreationException(String.format("Client with username: %s already exists.", authRequest.getUsername()));
        }
        Client client = new Client();
        client.setUsername(authRequest.getUsername());
        client.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        return clientRepository.save(client);
    }

    @Override
    public Client findByUserName(String username) {
        return clientRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Client with username: %s not found.", username)));
    }

    @Override
    public boolean existByUsername(String username) {
        return clientRepository.existsByUsername(username);
    }

}
