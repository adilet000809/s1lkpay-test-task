package com.example.authservice.service;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.model.Client;

public interface ClientService {

    Client addClient(AuthRequest authRequest);

    Client findByUserName(String username);

    boolean existByUsername(String username);

}
