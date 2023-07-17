package com.example.accountservice;

import com.example.accountservice.dto.AccountDto;
import com.example.accountservice.exception.EntityNotFoundException;
import com.example.accountservice.exception.ExistingEntityCreationException;
import com.example.accountservice.exception.InvalidAmountException;
import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;
import com.example.accountservice.service.AccountService;
import com.example.accountservice.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AccountServiceApplicationTests {

    @Mock
    private AccountRepository accountRepository;

    private AccountService accountService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        accountService = new AccountServiceImpl(accountRepository);
    }

    @Test
    public void testTopUpAccount_Success() {
        Account account = new Account(1L, "KZ1",  BigDecimal.ZERO, 1L);
        when(accountRepository.findByAccountNumber("KZ1")).thenReturn(Optional.of(account));

        BigDecimal amount = BigDecimal.valueOf(1000.0);
        AccountDto accountDto = new AccountDto("KZ1", amount);
        accountService.topUpAccount(accountDto);

        verify(accountRepository).save(account);
        assertEquals(amount, account.getBalance());
    }

    @Test
    public void testWithdrawAccount_Success() {
        BigDecimal initialBalance = BigDecimal.valueOf(2000.0);
        Account account = new Account(1L, "KZ1",  initialBalance, 1L);
        when(accountRepository.findByAccountNumberAndClientId("KZ1", 1L)).thenReturn(Optional.of(account));

        BigDecimal withdrawalAmount = BigDecimal.valueOf(1000.0);
        AccountDto accountDto = new AccountDto("KZ1", withdrawalAmount);
        accountService.withdraw(accountDto, account.getClientId());

        verify(accountRepository).save(account);
        BigDecimal expectedBalance = initialBalance.subtract(withdrawalAmount);
        assertEquals(expectedBalance, account.getBalance());
    }

    @Test
    public void testWithdraw_InsufficientFunds() {
        BigDecimal initialBalance = BigDecimal.valueOf(1000.0);
        BigDecimal withdrawalAmount = BigDecimal.valueOf(2000.0);
        Account account = new Account(1L, "KZ1",  initialBalance, 1L);
        when(accountRepository.findByAccountNumberAndClientId("KZ1", 1L)).thenReturn(Optional.of(account));

        AccountDto accountDto = new AccountDto("KZ1", withdrawalAmount);
        InvalidAmountException exception = assertThrows(InvalidAmountException.class, () ->
                accountService.withdraw(accountDto, account.getClientId()));

        assertEquals("Insufficient funds", exception.getMessage());
    }

    @Test
    public void testWithdraw_AccountNotFound() {
        BigDecimal withdrawalAmount = BigDecimal.valueOf(100.0);
        when(accountRepository.findByAccountNumberAndClientId("KZ1", 1L)).thenReturn(Optional.empty());

        AccountDto accountDto = new AccountDto("KZ1", withdrawalAmount);
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                accountService.withdraw(accountDto, 1L));

        assertEquals("Account with number: KZ1 not found.", exception.getMessage());
    }

    @Test
    public void testCreateAccount_Success() {
        Long clientId = 1L;
        AccountDto accountDto = new AccountDto("KZ1", BigDecimal.valueOf(1000.0));
        when(accountRepository.existsByAccountNumber(accountDto.getAccountNumber())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(new Account(1L, accountDto.getAccountNumber(), accountDto.getAmount(), clientId));
        Account createdAccount = accountService.createAccount(accountDto, clientId);

        assertNotNull(createdAccount);
        assertEquals(accountDto.getAccountNumber(), createdAccount.getAccountNumber());
        assertEquals(accountDto.getAmount(), createdAccount.getBalance());
        assertEquals(clientId, createdAccount.getClientId());
        verify(accountRepository, times(1)).existsByAccountNumber(accountDto.getAccountNumber());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void testCreateAccount_AccountAlreadyExists() {
        Long clientId = 1L;
        AccountDto accountDto = new AccountDto("KZ1", BigDecimal.valueOf(100.0));
        when(accountRepository.existsByAccountNumber(accountDto.getAccountNumber())).thenReturn(true);

        ExistingEntityCreationException exception = assertThrows(ExistingEntityCreationException.class, () ->
                accountService.createAccount(accountDto, clientId));

        assertEquals("Account with number: KZ1 already exists.", exception.getMessage());
        verify(accountRepository, times(1)).existsByAccountNumber(accountDto.getAccountNumber());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testGetAccountBalance_Success() {
        BigDecimal expectedBalance = BigDecimal.valueOf(1000.0);
        Account account = new Account(1L, "KZ1", expectedBalance, 1L);
        account.setBalance(expectedBalance);
        when(accountRepository.findByAccountNumberAndClientId("KZ1", 1L)).thenReturn(Optional.of(account));

        BigDecimal actualBalance = accountService.getAccountBalance("KZ1", 1L);

        assertEquals(expectedBalance, actualBalance);
        verify(accountRepository, times(1)).findByAccountNumberAndClientId("KZ1", 1L);
    }

}
