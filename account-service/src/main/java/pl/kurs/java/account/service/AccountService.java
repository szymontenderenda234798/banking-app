package pl.kurs.java.account.service;

import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import pl.kurs.java.account.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import pl.kurs.java.account.exception.AccountWithGivenPeselAlreadyExistsException;
import pl.kurs.java.account.exception.CurrentPeselNotMatchingException;
import pl.kurs.java.account.model.Account;
import pl.kurs.java.account.model.SubAccount;
import pl.kurs.java.account.model.command.CreateAccountCommand;
import pl.kurs.java.account.model.command.UpdateAccountCommand;
import org.springframework.stereotype.Service;
import pl.kurs.java.account.model.dto.AccountDto;
import pl.kurs.java.account.repository.AccountRepository;
import pl.kurs.java.exchange.config.SupportedCurrenciesConfig;


import java.math.BigDecimal;
import java.security.SecureRandom;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final SupportedCurrenciesConfig supportedCurrenciesConfig;

    @PostConstruct
    public void init() {
        System.out.println("Supported currencies: " + supportedCurrenciesConfig.getBaseCurrency() + " " + supportedCurrenciesConfig.getForeignCurrencies());
        Account a1 = new Account("12345678901", "Jan", "Kowalski", BigDecimal.valueOf(1000), "12345678901234567890123456", "PLN");
        Account a2 = new Account("12332112345", "Anna", "Nowak", BigDecimal.valueOf(2000), "12345678901234567890123457", "PLN");
        SubAccount sa1 = new SubAccount(BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");
        a1.addSubAccount(sa1);
        accountRepository.save(a1);
        accountRepository.save(a2);
    }

    @Transactional(readOnly = true)
    public Page<AccountDto> getAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(AccountDto::fromAccount);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "accounts", key = "#pesel")
    public AccountDto getAccount(String pesel) {
        Account account = accountRepository.findByPesel(pesel)
                .orElseThrow(() -> new AccountNotFoundException(pesel));
        System.out.println("Account: " + account);
        return AccountDto.fromAccount(account);
    }

    @Transactional
    public AccountDto updateAccount(String currentPesel, UpdateAccountCommand updateAccountCommand) {
        if (!currentPesel.equals(updateAccountCommand.currentPesel())) {
            throw new CurrentPeselNotMatchingException(currentPesel, updateAccountCommand.currentPesel());
        }
        Account account = accountRepository.findByPesel(currentPesel)
                .orElseThrow(() -> new AccountNotFoundException(currentPesel));
        account.setName(updateAccountCommand.newName());
        account.setSurname(updateAccountCommand.newSurname());
        Account updatedAccount = accountRepository.save(account);
        return AccountDto.fromAccount(updatedAccount);
    }

//    @Transactional
//    public AccountDto createAccount(CreateAccountCommand createAccountCommand) {
//        if (accountRepository.findByPesel(createAccountCommand.pesel()).isPresent()) {
//            throw new AccountWithGivenPeselAlreadyExistsException();
//        }
//        Account account = accountRepository.save(new Account(
//                createAccountCommand.pesel(),
//                createAccountCommand.name(),
//                createAccountCommand.surname(),
//                generateUniqueAccountNumber(),
//                createAccountCommand.startingBalance()));
//        return AccountDto.fromAccount(account);
//    }

    @Transactional
    public void deleteAccount(String pesel) {
        Account account = accountRepository.findByPesel(pesel)
                .orElseThrow(() -> new AccountNotFoundException(pesel));
        accountRepository.delete(account);
    }

    String generateUniqueAccountNumber() {
        String accountNumber = generateRandomAccountNumber();
        while (!isAccountNumberUnique(accountNumber)) {
            accountNumber = generateRandomAccountNumber();
        }
        return accountNumber;
    }

    String generateRandomAccountNumber() {
        SecureRandom random = new SecureRandom();
        StringBuilder number = new StringBuilder();

        // Ensure the first digit is not zero
        number.append(random.nextInt(9) + 1);

        // Append the remaining 25 digits
        for (int i = 0; i < 25; i++) {
            number.append(random.nextInt(10));
        }

        return number.toString();
    }

    boolean isAccountNumberUnique(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber).isEmpty();
    }
}
