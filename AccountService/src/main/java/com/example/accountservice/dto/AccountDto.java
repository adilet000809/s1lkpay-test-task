package com.example.accountservice.dto;

import java.math.BigDecimal;

public class AccountDto {

    private String accountNumber;
    private BigDecimal amount;

    public AccountDto(String accountNumber, BigDecimal amount) {
        this.accountNumber = accountNumber;
        this.amount = (amount != null) ? amount : BigDecimal.ZERO;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
