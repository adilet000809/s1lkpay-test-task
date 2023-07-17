package com.example.accountservice.service.impl;

import com.example.accountservice.dto.AccountDto;
import com.example.accountservice.exception.EntityNotFoundException;
import com.example.accountservice.exception.ExistingEntityCreationException;
import com.example.accountservice.exception.InvalidAmountException;
import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(AccountDto accountDto, Long clientId) {
        //Checking for existence of an account to eliminate account number duplication in DB
        if (accountRepository.existsByAccountNumber(accountDto.getAccountNumber())) {
            logger.info("Attempt to recreate existing account.");
            //Will be caught be RestExceptionHandler with proper error message
            throw new ExistingEntityCreationException(String.format("Account with number: %s already exists.", accountDto.getAccountNumber()));
        }
        //Creation an Account entity
        Account account = new Account();
        account.setAccountNumber(accountDto.getAccountNumber());
        account.setBalance(accountDto.getAmount());
        account.setClientId(clientId);
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account topUpAccount(AccountDto accountDto) {
        Account account = accountRepository.findByAccountNumber(accountDto.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Account with number: %s not found.", accountDto.getAccountNumber())));
        BigDecimal currentBalance = account.getBalance();
        BigDecimal topUpAmount = accountDto.getAmount();
        if (topUpAmount.compareTo(new BigDecimal(0)) <= 0) {
            logger.info("Negative or zero amount top-up");
            throw new InvalidAmountException("Invalid amount");
        }
        account.setBalance(currentBalance.add(accountDto.getAmount()));
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public void withdraw(AccountDto accountDto, Long clientId) {
        //Account is search as per the account number and client ID provided by AccountController
        //Client ID is used to check the ownership of an account by the authorized client to withdraw funds
        Account account = accountRepository.findByAccountNumberAndClientId(accountDto.getAccountNumber(), clientId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Account with number: %s not found.", accountDto.getAccountNumber())));
        BigDecimal currentBalance = account.getBalance();
        BigDecimal withdrawAmount = accountDto.getAmount();
        if (withdrawAmount.compareTo(new BigDecimal(0)) <= 0) {
            //Check if the withdraw amount is less or equal to zero and throw an exception which will be caught by the RestException handler
            throw new InvalidAmountException("Invalid amount");
        }
        if (currentBalance.compareTo(withdrawAmount) < 0) {
            throw new InvalidAmountException("Insufficient funds");
        }
        account.setBalance(currentBalance.subtract(withdrawAmount));
        accountRepository.save(account);
    }

    @Override
    public BigDecimal getAccountBalance(String accountNumber, Long clientId) {
        //Only the account owners can see their account balance
        Account account = accountRepository.findByAccountNumberAndClientId(accountNumber, clientId)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Account with number: %s not found.", accountNumber)));
        return account.getBalance();
    }

}
