package com.example.accountservice.service;

import com.example.accountservice.dto.AccountDto;
import com.example.accountservice.model.Account;

import java.math.BigDecimal;

public interface AccountService {

    Account createAccount(AccountDto accountDto, Long clientId);

    Account topUpAccount(AccountDto accountDto);

    void withdraw(AccountDto accountDto, Long clientId);

    BigDecimal getAccountBalance(String accountNumber, Long clientId);

}
