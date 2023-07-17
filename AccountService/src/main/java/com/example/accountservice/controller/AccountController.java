package com.example.accountservice.controller;

import com.example.accountservice.dto.AccountDto;
import com.example.accountservice.dto.FundTransferRequest;
import com.example.accountservice.model.Account;
import com.example.accountservice.service.AccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/account")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(HttpServletRequest request, @RequestBody AccountDto accountDto) {
        //Fetching the client's ID from request attribute set in JwtFilter.java class
        Long clientId = (Long) request.getAttribute("clientId");
        if (clientId == null) {
            logger.info("Unauthorized access. Client id not found. Attempt to create an account without authorization.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Account createdAccount = accountService.createAccount(accountDto, clientId);
        logger.info(String.format("Client with id: %d created an account successfully.", clientId));
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(HttpServletRequest request, @PathVariable String accountNumber) {
        //Fetching the client's ID from request attribute set in JwtFilter.java class
        Long clientId = (Long) request.getAttribute("clientId");
        if (clientId == null) {
            logger.info("Unauthorized access. Client id not found. Attempt to retrieve balance without authorization.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BigDecimal balance = accountService.getAccountBalance(accountNumber, clientId);
        return ResponseEntity.ok(balance);
    }

    @PostMapping("/top-up")
    public ResponseEntity<Account> topUpAccount(@RequestBody AccountDto accountDto) {
        //Balance top-up
        Account account = accountService.topUpAccount(accountDto);
        return ResponseEntity.ok(account);
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transferFunds(HttpServletRequest request, @RequestBody FundTransferRequest fundTransferRequest) {
        //Fetching the client's ID from request attribute set in JwtFilter.java class
        //Client's ID will be used to check whether withdrawal account belongs to authorized client
        Long clientId = (Long) request.getAttribute("clientId");
        if (clientId == null) {
            logger.info("Unauthorized access. Client id not found. Attempt to transfer funds without authorization.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Map<String, Object> response = new HashMap<>();
        if (fundTransferRequest.getFromAccountNumber().equals(fundTransferRequest.getToAccountNumber())) {
            logger.info("Attempt to transfer fund between the same accounts blocked.");
            response.put("messages", "Self transfer");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        accountService.withdraw(new AccountDto(
                fundTransferRequest.getFromAccountNumber(), fundTransferRequest.getAmount()), clientId);
        accountService.topUpAccount(new AccountDto(
                fundTransferRequest.getToAccountNumber(), fundTransferRequest.getAmount()
        ));
        response.put("messages", "Transfer successful");
        return ResponseEntity.ok(response);
    }

}
