package pl.kurs.java.account.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.kurs.java.account.exception.AccountNotFoundException;
import pl.kurs.java.account.exception.CurrentPeselNotMatchingException;
import pl.kurs.java.account.model.Account;
import pl.kurs.java.account.model.command.UpdateAccountCommand;
import pl.kurs.java.account.model.dto.AccountDto;
import pl.kurs.java.account.repository.AccountRepository;
import pl.kurs.java.account.model.command.CreateAccountCommand;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAccount_ShouldReturnAccountDto_WhenAccountExists() {
        // given
        String pesel = "12345678901";
        Account account = new Account(pesel, "John", "Doe", "12345678901234567890123456", 1000.0, 0);
        when(accountRepository.findById(pesel)).thenReturn(Optional.of(account));

        // when
        AccountDto accountDto = accountService.getAccount(pesel);

        // then
        assertNotNull(accountDto);
        assertEquals(account.getPesel(), accountDto.pesel());
        assertEquals(account.getName(), accountDto.name());
        assertEquals(account.getSurname(), accountDto.surname());
        assertEquals(account.getAccountNumber(), accountDto.accountNumber());
        assertEquals(account.getPlnBalance(), accountDto.plnBalance());
        assertEquals(account.getUsdBalance(), accountDto.usdBalance());
    }

    @Test
    void testGetAccount_ShouldThrowAccountNotFoundException_WhenAccountDoesNotExist() {
        // given
        String pesel = "12345678901";
        when(accountRepository.findById(pesel)).thenReturn(Optional.empty());

        // when & then
        assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(pesel));
    }


    @Test
    void testCreateAccount_ShouldSaveAccountAndReturnAccountDto() {
        // given
        String pesel = "12345678901";
        String name = "John";
        String surname = "Doe";
        String accountNumber = "12345678901234567890123456";
        double plnBalance = 1000.0;
        double usdBalance = 0;
        CreateAccountCommand createAccountCommand = new CreateAccountCommand(pesel, name, surname, plnBalance);

        Account account = new Account(pesel, name, surname, accountNumber, plnBalance, usdBalance);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        when(accountRepository.findById(anyString())).thenReturn(Optional.empty()); // For unique account number check

        // when
        AccountDto accountDto = accountService.createAccount(createAccountCommand);

        // then
        assertNotNull(accountDto);
        assertEquals(pesel, accountDto.pesel());
        assertEquals(name, accountDto.name());
        assertEquals(surname, accountDto.surname());
        assertEquals(accountNumber, accountDto.accountNumber());
        assertEquals(plnBalance, accountDto.plnBalance());
        assertEquals(usdBalance, accountDto.usdBalance());

        ArgumentCaptor<Account> accountArgumentCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository, times(1)).save(accountArgumentCaptor.capture());
        assertEquals(pesel, accountArgumentCaptor.getValue().getPesel());
        assertEquals(name, accountArgumentCaptor.getValue().getName());
        assertEquals(surname, accountArgumentCaptor.getValue().getSurname());
        assertEquals(plnBalance, accountArgumentCaptor.getValue().getPlnBalance());
    }


    @Test
    void testGenerateUniqueAccountNumber_ShouldRetryUntilUniqueNumberIsGenerated() {
        // given
        String nonUniqueAccountNumber1 = "12345678901234567890123456";
        String nonUniqueAccountNumber2 = "65432109876543210987654321";
        when(accountRepository.findById(nonUniqueAccountNumber1)).thenReturn(Optional.of(new Account()));
        when(accountRepository.findById(nonUniqueAccountNumber2)).thenReturn(Optional.of(new Account()));
        when(accountRepository.findById(anyString())).thenAnswer(invocation -> {
            String argument = invocation.getArgument(0);
            if (argument.equals(nonUniqueAccountNumber1) || argument.equals(nonUniqueAccountNumber2)) {
                return Optional.of(new Account());
            } else {
                return Optional.empty();
            }
        });

        // when
        String accountNumber = accountService.generateUniqueAccountNumber();

        // then
        assertNotNull(accountNumber);
        assertEquals(26, accountNumber.length());
        assertTrue(accountNumber.matches("\\d{26}"));
        assertNotEquals(nonUniqueAccountNumber1, accountNumber);
        assertNotEquals(nonUniqueAccountNumber2, accountNumber);
    }

    @Test
    void testGenerateRandomAccountNumber_ShouldGenerate26DigitNumber() {
        // when
        String accountNumber = accountService.generateRandomAccountNumber();

        // then
        assertNotNull(accountNumber, "Account number should not be null");
        assertEquals(26, accountNumber.length(), "Account number should be 26 digits long");
        assertTrue(accountNumber.matches("\\d{26}"), "Account number should only contain digits");
        assertNotEquals('0', accountNumber.charAt(0), "The first digit of account number should not be zero");
    }

    @Test
    void testIsAccountNumberUnique_ShouldReturnTrue_WhenAccountNumberIsUnique() {
        // Given
        String uniqueAccountNumber = "12345678901234567890123456";
        when(accountRepository.findByAccountNumber(uniqueAccountNumber)).thenReturn(Optional.empty());

        // When
        boolean isUnique = accountService.isAccountNumberUnique(uniqueAccountNumber);

        // Then
        assertTrue(isUnique, "Account number should be unique");
    }

    @Test
    void testIsAccountNumberUnique_ShouldReturnFalse_WhenAccountNumberIsNotUnique() {
        // Given
        String nonUniqueAccountNumber = "12345678901234567890123456";
        Account existingAccount = new Account(nonUniqueAccountNumber, "John", "Doe", nonUniqueAccountNumber, 1000.0, 0);
        when(accountRepository.findByAccountNumber(nonUniqueAccountNumber)).thenReturn(Optional.of(existingAccount));

        // When
        boolean isUnique = accountService.isAccountNumberUnique(nonUniqueAccountNumber);

        // Then
        assertFalse(isUnique, "Account number should not be unique");
    }

    @Test
    void testGetAccounts_ShouldReturnPagedAccounts() {
        // given
        Pageable pageable = PageRequest.of(0, 2);
        Account account1 = new Account("12345678901", "John", "Doe", "12345678901234567890123456", 1000.0, 0);
        Account account2 = new Account("98765432109", "Jane", "Doe", "65432109876543210987654321", 2000.0, 0);
        List<Account> accounts = Arrays.asList(account1, account2);
        Page<Account> accountPage = new PageImpl<>(accounts, pageable, accounts.size());

        when(accountRepository.findAll(pageable)).thenReturn(accountPage);

        // when
        Page<AccountDto> accountDtos = accountService.getAccounts(pageable);

        // then
        assertNotNull(accountDtos);
        assertEquals(2, accountDtos.getTotalElements());
        assertEquals(2, accountDtos.getContent().size());
        assertEquals("12345678901", accountDtos.getContent().get(0).pesel());
        assertEquals("98765432109", accountDtos.getContent().get(1).pesel());
    }


    @Test
    void testUpdateAccount_ShouldUpdateSuccessfully_WhenPeselMatches() {
        // given
        String currentPesel = "12345678901";
        UpdateAccountCommand updateCommand = new UpdateAccountCommand(currentPesel, currentPesel, "NewName", "NewSurname");
        Account existingAccount = new Account(currentPesel, "OldName", "OldSurname", "accountNumber", 1000.0, 0);

        when(accountRepository.findById(currentPesel)).thenReturn(Optional.of(existingAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(existingAccount);

        // when
        AccountDto updatedAccountDto = accountService.updateAccount(currentPesel, updateCommand);

        // then
        assertNotNull(updatedAccountDto);
        assertEquals(updateCommand.newPesel(), updatedAccountDto.pesel());
        assertEquals(updateCommand.newName(), updatedAccountDto.name());
        assertEquals(updateCommand.newSurname(), updatedAccountDto.surname());

        verify(accountRepository, times(1)).findById(currentPesel);
        verify(accountRepository, times(1)).save(existingAccount);
    }

    @Test
    void testUpdateAccount_ShouldThrowAccountNotFoundException_WhenAccountDoesNotExist() {
        // given
        String currentPesel = "12345678901";
        UpdateAccountCommand updateCommand = new UpdateAccountCommand(currentPesel, currentPesel, "NewName", "NewSurname");

        when(accountRepository.findById(currentPesel)).thenReturn(Optional.empty());

        // when & then
        assertThrows(AccountNotFoundException.class, () -> accountService.updateAccount(currentPesel, updateCommand));

        verify(accountRepository, times(1)).findById(currentPesel);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testUpdateAccount_ShouldThrowCurrentPeselNotMatchingException_WhenPeselDoesNotMatch() {
        // given
        String currentPeselFromPathVariable = "12345678901";
        String currentPeselFromCommand = "98765432109";
        String newPesel = "11122233344";
        UpdateAccountCommand updateCommand = new UpdateAccountCommand(currentPeselFromCommand, newPesel, "NewName", "NewSurname");

        // When & Then
        assertThrows(CurrentPeselNotMatchingException.class, () -> accountService.updateAccount(currentPeselFromPathVariable, updateCommand));

        verify(accountRepository, never()).findById(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testDeleteAccount_ShouldDeleteSuccessfully_WhenAccountExists() {
        // given
        String pesel = "12345678901";
        Account existingAccount = new Account(pesel, "John", "Doe", "accountNumber", 1000.0, 0);

        when(accountRepository.findById(pesel)).thenReturn(Optional.of(existingAccount));

        // when
        accountService.deleteAccount(pesel);

        // then
        verify(accountRepository, times(1)).findById(pesel);
        verify(accountRepository, times(1)).delete(existingAccount);
    }

    @Test
    void testDeleteAccount_ShouldThrowAccountNotFoundException_WhenAccountDoesNotExist() {
        // given
        String pesel = "12345678901";

        when(accountRepository.findById(pesel)).thenReturn(Optional.empty());

        // when & then
        assertThrows(AccountNotFoundException.class, () -> accountService.deleteAccount(pesel));

        verify(accountRepository, times(1)).findById(pesel);
        verify(accountRepository, never()).delete(any(Account.class));
    }
}