package com.example.accountservice;

import com.example.accountservice.dto.AccountDto;
import com.example.accountservice.dto.AuthRequest;
import com.example.accountservice.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AccountServiceIntegrationTests {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void testClientIdRetrieve_Success() {
        //Create a user with such credentials before running the test
        String username = "adilet";
        String password = "Adilet.123";
        AuthRequest authRequest = new AuthRequest(username, password);
        String jwt = getJwt(authRequest);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + jwt);

        //By sending validation request with JWT, AccountService receives the ID of a client as per the JWT sent
        //The client ID will be used during the creation of an Account entity in DB in Account microservice.
        ResponseEntity<Long> clientCreationResponse = restTemplate.exchange(
                "http://localhost:8080/api/auth/validate",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Long.class
        );
        assertEquals(HttpStatus.OK, clientCreationResponse.getStatusCode());
        assertNotNull(clientCreationResponse.getBody());
        Long clientId = clientCreationResponse.getBody();

        String accountNumber = "KZ10";
        BigDecimal initialBalance = BigDecimal.valueOf(1000);
        AccountDto accountDto = new AccountDto(accountNumber, initialBalance);

        HttpEntity<AccountDto> accountCreationRequest = new HttpEntity<>(accountDto, headers);
        //Request to create an Account entity. By putting JWT in request header Account service will refer to
        //Auth service to get the ID of client. As per the ID received, an Account entity will be created
        ResponseEntity<Account> accountCreationResponse = restTemplate.exchange(
                "http://localhost:8081/api/account",
                HttpMethod.POST,
                accountCreationRequest,
                Account.class
        );
        assertEquals(HttpStatus.CREATED, accountCreationResponse.getStatusCode());
        assertNotNull(accountCreationResponse.getBody());

        Account createdAccount = accountCreationResponse.getBody();

        assertEquals(createdAccount.getAccountNumber(), accountDto.getAccountNumber());
        assertEquals(createdAccount.getBalance(), accountDto.getAmount());
        assertEquals(createdAccount.getClientId(), clientId);

    }

    private String getJwt(AuthRequest authRequest) {
        Map authenticationRequest = restTemplate.postForObject(
                "http://localhost:8080/api/auth/login",
                authRequest,
                Map.class
        );
        return (String) authenticationRequest.get("token");

    }

}
