package com.example.authservice.controller;

import com.example.authservice.dto.AuthRequest;
import com.example.authservice.dto.ErrorResponse;
import com.example.authservice.jwt.JwtService;
import com.example.authservice.model.Client;
import com.example.authservice.service.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/auth")
public class AuthController {

    private final ClientService clientService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(ClientService clientService, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.clientService = clientService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody AuthRequest authRequest) {
        //Check if the client with this username already exist
        if (!clientService.existByUsername(authRequest.getUsername())) {
            if (isValidPassword(authRequest.getPassword())) {
                clientService.addClient(authRequest);
                Map<String, Object> response = new HashMap<>();
                response.put("messages", "Client has been registered successfully.");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(), "Password is too short and easy to guess."));
            }
        } else {
            return ResponseEntity.badRequest().body(new ErrorResponse(HttpStatus.BAD_REQUEST.value(),
                    HttpStatus.BAD_REQUEST.getReasonPhrase(), String.format("Client with username %s already exist.", authRequest.getUsername())));
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthRequest authRequest) {

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(), authRequest.getPassword()));
        Client client = clientService.findByUserName(authRequest.getUsername());
        String token = jwtService.generateToken(authRequest.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("user", client);
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    //A method for validation of JWT received from other microservices to provide client's data
    //So other microservices can refer to this endpoint to retrieve authorized user's data
    @GetMapping("/validate")
    public ResponseEntity<Long> validate() {
        return ResponseEntity.ok(getClientData().getId());
    }

    //Retrieve authenticated user's details
    private Client getClientData(){
        Client userData = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(!(authentication instanceof AnonymousAuthenticationToken)){
            String currentUserName = authentication.getName();
            userData = clientService.findByUserName(currentUserName);
        }
        return userData;
    }

    private boolean isValidPassword(String password) {
        return password.matches("^(?=.*[0-9]).{8,}$");
    }

}
