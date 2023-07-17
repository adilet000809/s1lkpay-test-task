package com.example.authservice.jwt;

import com.example.authservice.model.Client;
import com.example.authservice.repository.ClientRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JwtUserDetailsService implements UserDetailsService {

    private final ClientRepository repository;

    public JwtUserDetailsService(ClientRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Client> client = repository.findByUsername(username);
        if (client.isEmpty()) throw new UsernameNotFoundException("Client not found " + username);
        return new JwtUserDetails(client.get().getId(), client.get().getUsername(), client.get().getPassword());
    }

}
