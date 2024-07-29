package pl.kurs.java.account.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.kurs.java.account.exception.AccountNotFoundException;
import pl.kurs.java.account.exception.NoSubAccountInGivenCurrencyException;
import pl.kurs.java.account.exception.PeselFromPathVariableAndRequestBodyNotMatchingException;
import pl.kurs.java.account.model.account.Account;
import pl.kurs.java.account.model.account.SubAccount;
import pl.kurs.java.account.model.account.command.CreateAccountCommand;
import pl.kurs.java.account.model.account.command.CreateSubAccountCommand;
import pl.kurs.java.account.model.account.command.UpdateAccountCommand;
import pl.kurs.java.account.model.account.dto.AccountDto;
import pl.kurs.java.account.model.transaction.command.BuyForeignCurrencyCommand;
import pl.kurs.java.account.model.transaction.command.SellForeignCurrencyCommand;
import pl.kurs.java.account.model.transaction.enums.TransactionStatus;
import pl.kurs.java.account.model.transaction.request.BuyTransactionRequest;
import pl.kurs.java.account.model.transaction.request.SellTransactionRequest;
import pl.kurs.java.account.repository.AccountRepository;
import pl.kurs.java.account.repository.SubAccountRepository;
import pl.kurs.java.exchange.config.SupportedCurrenciesConfig;
import pl.kurs.java.exchange.service.ExchangeRateService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private SubAccountRepository subAccountRepository;

    @Mock
    private SupportedCurrenciesConfig supportedCurrenciesConfig;

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private AccountService accountService;

    private Account account;
    private SubAccount subAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        account = new Account("12345678901", "John", "Doe", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");
        subAccount = new SubAccount(BigDecimal.valueOf(1000), "USD", "12345678901234567890123456");
        account.addSubAccount(subAccount);
    }

    @Test
    void testGetAccounts() {
        Pageable pageable = PageRequest.of(0, 10);
        Account account1 = new Account("12345678901", "John", "Doe", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");
        Account account2 = new Account("98765432109", "Jane", "Smith", BigDecimal.valueOf(500), "PLN", "65432109876543210987654321");
        Page<Account> accounts = new PageImpl<>(List.of(account1, account2));

        when(accountRepository.findAll(pageable)).thenReturn(accounts);

        Page<AccountDto> result = accountService.getAccounts(pageable);

        assertEquals(2, result.getTotalElements());
        verify(accountRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetAccount() {
        String pesel = "12345678901";
        Account account = new Account(pesel, "John", "Doe", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");

        when(accountRepository.findByPesel(pesel)).thenReturn(Optional.of(account));

        AccountDto result = accountService.getAccount(pesel);

        assertEquals(pesel, result.pesel());
        verify(accountRepository, times(1)).findByPesel(pesel);
    }

    @Test
    void testCreateAccount() {
        CreateAccountCommand command = new CreateAccountCommand("12345678901", "John", "Doe", BigDecimal.valueOf(1000), "PLN");
        Account account = new Account(command.pesel(), command.name(), command.surname(), command.startingBalance(), command.currency(), "12345678901234567890123456");

        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountDto result = accountService.createAccount(command);

        assertEquals(command.pesel(), result.pesel());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testUpdateAccount() {
        String pesel = "12345678901";
        UpdateAccountCommand command = new UpdateAccountCommand(pesel, "Jane", "Doe");
        Account account = new Account(pesel, "John", "Doe", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");

        when(accountRepository.findByPesel(pesel)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountDto result = accountService.updateAccount(pesel, command);

        assertEquals(command.newName(), result.name());
        verify(accountRepository, times(1)).findByPesel(pesel);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testDeleteAccount() {
        String pesel = "12345678901";
        Account account = new Account(pesel, "John", "Doe", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");

        when(accountRepository.findByPesel(pesel)).thenReturn(Optional.of(account));

        accountService.deleteAccount(pesel);

        verify(accountRepository, times(1)).findByPesel(pesel);
        verify(accountRepository, times(1)).delete(account);
    }

    @Test
    void testCreateSubAccount() {
        String pesel = "12345678901";
        CreateSubAccountCommand command = new CreateSubAccountCommand("USD");
        Account account = new Account(pesel, "John", "Doe", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");

        when(accountRepository.findByPesel(pesel)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenReturn(account);

        AccountDto result = accountService.createSubAccount(pesel, command);

        assertTrue(result.subAccounts().stream().anyMatch(sa -> sa.currency().equals("USD")));
        verify(accountRepository, times(1)).findByPesel(pesel);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testDeleteSubAccount() {
        String pesel = "12345678901";
        String currency = "USD";
        Account account = new Account(pesel, "John", "Doe", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");
        SubAccount subAccount = new SubAccount(BigDecimal.valueOf(1000), currency, "12345678901234567890123456");
        account.addSubAccount(subAccount);

        when(accountRepository.findByPesel(pesel)).thenReturn(Optional.of(account));

        accountService.deleteSubAccount(pesel, currency);

        verify(accountRepository, times(1)).findByPesel(pesel);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void testBuyForeignCurrency() {
        String pesel = "12345678901";
        String currency = "USD";
        BuyForeignCurrencyCommand command = new BuyForeignCurrencyCommand(pesel, BigDecimal.valueOf(100), currency);
        Account account = new Account(pesel, "John", "Doe", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");
        account.addSubAccount(new SubAccount(BigDecimal.valueOf(1000), currency, "12345678901234567890123456"));

        when(accountRepository.findByPesel(pesel)).thenReturn(Optional.of(account));
        when(exchangeRateService.getExchangeRate(currency)).thenReturn(4.0);

        BuyTransactionRequest result = accountService.buyForeignCurrency(pesel, command);

        assertEquals(TransactionStatus.COMPLETED, result.transactionStatus());
        verify(accountRepository, times(1)).findByPesel(pesel);
    }

    @Test
    void testBuyForeignCurrency_InsufficientBalance() {
        // given
        BuyForeignCurrencyCommand command = new BuyForeignCurrencyCommand("12345678901", BigDecimal.valueOf(1000), "USD");
        when(accountRepository.findByPesel("12345678901")).thenReturn(Optional.of(account));
        when(exchangeRateService.getExchangeRate("USD")).thenReturn(4.0);

        // when
        BuyTransactionRequest result = accountService.buyForeignCurrency("12345678901", command);

        // then
        assertEquals(TransactionStatus.FAILED, result.transactionStatus());
        assertEquals(BigDecimal.valueOf(1000), account.getBalance());
        assertEquals(BigDecimal.valueOf(1000), subAccount.getBalance());
    }

    @Test
    void testBuyForeignCurrency_NoSubAccountInGivenCurrency() {
        // given
        BuyForeignCurrencyCommand command = new BuyForeignCurrencyCommand("12345678901", BigDecimal.valueOf(100), "EUR");
        when(accountRepository.findByPesel("12345678901")).thenReturn(Optional.of(account));

        // when
        assertThrows(NoSubAccountInGivenCurrencyException.class, () -> {
            accountService.buyForeignCurrency("12345678901", command);
        });
    }

    @Test
    void testBuyForeignCurrency_PeselMismatch() {
        // given
        BuyForeignCurrencyCommand command = new BuyForeignCurrencyCommand("wrongPesel", BigDecimal.valueOf(100), "USD");

        // when
        assertThrows(PeselFromPathVariableAndRequestBodyNotMatchingException.class, () -> {
            accountService.buyForeignCurrency("12345678901", command);
        });
    }

    @Test
    void testSellForeignCurrency() {
        String pesel = "12345678901";
        String currency = "USD";
        SellForeignCurrencyCommand command = new SellForeignCurrencyCommand(pesel, BigDecimal.valueOf(100), currency);
        Account account = new Account(pesel, "John", "Doe", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");
        account.addSubAccount(new SubAccount(BigDecimal.valueOf(1000), currency, "12345678901234567890123456"));

        when(accountRepository.findByPesel(pesel)).thenReturn(Optional.of(account));
        when(exchangeRateService.getExchangeRate(currency)).thenReturn(4.0);

        SellTransactionRequest result = accountService.sellForeignCurrency(pesel, command);

        assertEquals(TransactionStatus.COMPLETED, result.transactionStatus());
        verify(accountRepository, times(1)).findByPesel(pesel);
    }

    @Test
    void testSellForeignCurrency_InsufficientBalance() {
        // given
        SellForeignCurrencyCommand command = new SellForeignCurrencyCommand("12345678901", BigDecimal.valueOf(2000), "USD");
        when(accountRepository.findByPesel("12345678901")).thenReturn(Optional.of(account));
        when(exchangeRateService.getExchangeRate("USD")).thenReturn(4.0);

        // when
        SellTransactionRequest result = accountService.sellForeignCurrency("12345678901", command);

        // then
        assertEquals(TransactionStatus.FAILED, result.transactionStatus());
        assertEquals(BigDecimal.valueOf(1000), account.getBalance());
        assertEquals(BigDecimal.valueOf(1000), subAccount.getBalance());
    }

    @Test
    void testSellForeignCurrency_NoSubAccountInGivenCurrency() {
        // given
        SellForeignCurrencyCommand command = new SellForeignCurrencyCommand("12345678901", BigDecimal.valueOf(100), "EUR");
        when(accountRepository.findByPesel("12345678901")).thenReturn(Optional.of(account));

        // when
        assertThrows(NoSubAccountInGivenCurrencyException.class, () -> {
            accountService.sellForeignCurrency("12345678901", command);
        });
    }

    @Test
    void testSellForeignCurrency_PeselMismatch() {
        // given
        SellForeignCurrencyCommand command = new SellForeignCurrencyCommand("wrongPesel", BigDecimal.valueOf(100), "USD");

        // when
        assertThrows(PeselFromPathVariableAndRequestBodyNotMatchingException.class, () -> {
            accountService.sellForeignCurrency("12345678901", command);
        });
    }
}