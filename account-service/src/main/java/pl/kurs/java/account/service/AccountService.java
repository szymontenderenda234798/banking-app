package pl.kurs.java.account.service;

import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import pl.kurs.java.account.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import pl.kurs.java.account.exception.AccountWithGivenPeselAlreadyExistsException;
import pl.kurs.java.account.exception.NoSubAccountInGivenCurrencyException;
import pl.kurs.java.account.exception.PeselFromPathVariableAndRequestBodyNotMatchingException;
import pl.kurs.java.account.model.account.Account;
import pl.kurs.java.account.model.account.SubAccount;
import pl.kurs.java.account.model.account.command.CreateAccountCommand;
import pl.kurs.java.account.model.account.command.UpdateAccountCommand;
import org.springframework.stereotype.Service;
import pl.kurs.java.account.model.account.command.CreateSubAccountCommand;
import pl.kurs.java.account.model.transaction.command.BuyForeignCurrencyCommand;
import pl.kurs.java.account.model.account.dto.AccountDto;
import pl.kurs.java.account.model.transaction.command.SellForeignCurrencyCommand;
import pl.kurs.java.account.model.transaction.dto.TransactionResultDto;
import pl.kurs.java.account.model.transaction.enums.TransactionStatus;
import pl.kurs.java.account.model.transaction.request.BuyTransactionRequest;
import pl.kurs.java.account.model.transaction.request.SellTransactionRequest;
import pl.kurs.java.account.repository.AccountRepository;
import pl.kurs.java.account.repository.SubAccountRepository;
import pl.kurs.java.exchange.config.SupportedCurrenciesConfig;
import pl.kurs.java.exchange.service.ExchangeRateService;


import java.math.BigDecimal;
import java.security.SecureRandom;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final SubAccountRepository subAccountRepository;
    private final SupportedCurrenciesConfig supportedCurrenciesConfig;
    private final ExchangeRateService exchangeRateService;

    @PostConstruct
    public void init() {
        System.out.println("Supported currencies: " + supportedCurrenciesConfig.getBaseCurrency() + " " + supportedCurrenciesConfig.getForeignCurrencies());
        Account a1 = new Account("12345678901", "Jan", "Kowalski", BigDecimal.valueOf(1000), "PLN", "12345678901234567890123456");
        Account a2 = new Account("12332112345", "Anna", "Nowak", BigDecimal.valueOf(2000), "PLN", "12345678901234567890123457");
        SubAccount sa1 = new SubAccount(BigDecimal.valueOf(0), "USD", "12345678901234567890123456");
        subAccountRepository.save(sa1);
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
            throw new PeselFromPathVariableAndRequestBodyNotMatchingException(currentPesel, updateAccountCommand.currentPesel());
        }
        Account account = accountRepository.findByPesel(currentPesel)
                .orElseThrow(() -> new AccountNotFoundException(currentPesel));
        account.setName(updateAccountCommand.newName());
        account.setSurname(updateAccountCommand.newSurname());
        Account updatedAccount = accountRepository.save(account);
        return AccountDto.fromAccount(updatedAccount);
    }

    @Transactional
    public AccountDto createAccount(CreateAccountCommand createAccountCommand) {
        if (accountRepository.findByPesel(createAccountCommand.pesel()).isPresent()) {
            throw new AccountWithGivenPeselAlreadyExistsException();
        }
        Account account = accountRepository.save(new Account(
                createAccountCommand.pesel(),
                createAccountCommand.name(),
                createAccountCommand.surname(),
                createAccountCommand.startingBalance(),
                createAccountCommand.currency(),
                generateUniqueAccountNumber()));
        return AccountDto.fromAccount(account);
    }

    @Transactional
    public void deleteAccount(String pesel) {
        Account account = accountRepository.findByPesel(pesel)
                .orElseThrow(() -> new AccountNotFoundException(pesel));
        accountRepository.delete(account);
    }

    @Transactional
    public AccountDto createSubAccount(String pesel, CreateSubAccountCommand createSubAccountCommand) {
        Account account = accountRepository.findByPesel(pesel)
                .orElseThrow(() -> new AccountNotFoundException(pesel));
        SubAccount subAccount = new SubAccount(BigDecimal.ZERO, createSubAccountCommand.currency(), generateUniqueAccountNumber());
        account.addSubAccount(subAccount);
        accountRepository.save(account);
        return AccountDto.fromAccount(account);
    }

    @Transactional
    public void deleteSubAccount(String pesel, String currency) {
        Account account = accountRepository.findByPesel(pesel)
                .orElseThrow(() -> new AccountNotFoundException(pesel));
        SubAccount subAccount = account.getSubAccounts().stream()
                .filter(sa -> sa.getCurrency().equals(currency))
                .findFirst()
                .orElseThrow(NoSubAccountInGivenCurrencyException::new);
        account.getSubAccounts().remove(subAccount);
        accountRepository.save(account);
    }

    @Transactional
    public BuyTransactionRequest buyForeignCurrency(String pesel, BuyForeignCurrencyCommand buyForeignCurrencyCommand) {
        if (!pesel.equals(buyForeignCurrencyCommand.pesel())) {
            throw new PeselFromPathVariableAndRequestBodyNotMatchingException(pesel, buyForeignCurrencyCommand.pesel());
        }
        Account account = accountRepository.findByPesel(pesel)
                .orElseThrow(() -> new AccountNotFoundException(pesel));
        if (!account.hasAccountInGivenCurrency(buyForeignCurrencyCommand.currency())) {
            throw new NoSubAccountInGivenCurrencyException();
        }

        double exchangeRate = exchangeRateService.getExchangeRate(buyForeignCurrencyCommand.currency());
        BigDecimal amountToSpend = buyForeignCurrencyCommand.amountToBuy().multiply(BigDecimal.valueOf(exchangeRate)).setScale(2, BigDecimal.ROUND_HALF_UP);

        BuyTransactionRequest buyTransactionRequest = new BuyTransactionRequest(
                pesel,
                buyForeignCurrencyCommand.currency(),
                buyForeignCurrencyCommand.amountToBuy(),
                BigDecimal.valueOf(exchangeRate),
                account.getCurrency(),
                amountToSpend,
                TransactionStatus.CREATED
        );

        return account.buyForeignCurrency(buyTransactionRequest);
    }

    @Transactional
    public SellTransactionRequest sellForeignCurrency(String pesel, SellForeignCurrencyCommand sellForeignCurrencyCommand) {
        if (!pesel.equals(sellForeignCurrencyCommand.pesel())) {
            throw new PeselFromPathVariableAndRequestBodyNotMatchingException(pesel, sellForeignCurrencyCommand.pesel());
        }
        Account account = accountRepository.findByPesel(pesel)
                .orElseThrow(() -> new AccountNotFoundException(pesel));
        if (!account.hasAccountInGivenCurrency(sellForeignCurrencyCommand.currency())) {
            throw new NoSubAccountInGivenCurrencyException();
        }

        double exchangeRate = exchangeRateService.getExchangeRate(sellForeignCurrencyCommand.currency());
        BigDecimal amountToReceive = sellForeignCurrencyCommand.amountToSell().multiply(BigDecimal.valueOf(exchangeRate)).setScale(2, BigDecimal.ROUND_HALF_UP);

        SellTransactionRequest sellTransactionRequest = new SellTransactionRequest(
                pesel,
                sellForeignCurrencyCommand.currency(),
                sellForeignCurrencyCommand.amountToSell(),
                BigDecimal.valueOf(exchangeRate),
                account.getCurrency(),
                amountToReceive,
                TransactionStatus.CREATED
        );

        return account.sellForeignCurrency(sellTransactionRequest);
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
