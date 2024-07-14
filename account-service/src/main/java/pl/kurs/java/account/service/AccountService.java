package pl.kurs.java.account.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.kurs.java.account.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import pl.kurs.java.account.exception.AccountWithGivenPeselAlreadyExistsException;
import pl.kurs.java.account.exception.CurrentPeselNotMatchingException;
import pl.kurs.java.account.model.Account;
import pl.kurs.java.account.model.command.CreateAccountCommand;
import pl.kurs.java.account.model.command.UpdateAccountCommand;
import pl.kurs.java.account.model.dto.AccountDto;
import org.springframework.stereotype.Service;
import pl.kurs.java.account.repository.AccountRepository;
import pl.kurs.java.account.service.producer.ExchangeResponseProducer;
import pl.kurs.java.client.model.ExchangeRequestDto;
import pl.kurs.java.client.model.ExchangeResponseDto;
import pl.kurs.java.client.model.enums.ExchangeStatus;

import java.security.SecureRandom;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ExchangeResponseProducer exchangeResponseProducer;

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

    @Transactional
    public AccountDto createAccount(CreateAccountCommand createAccountCommand) {
        if (accountRepository.findByPesel(createAccountCommand.pesel()).isPresent()) {
            throw new AccountWithGivenPeselAlreadyExistsException();
        }
        Account account = accountRepository.save(new Account(
                createAccountCommand.pesel(),
                createAccountCommand.name(),
                createAccountCommand.surname(),
                generateUniqueAccountNumber(),
                createAccountCommand.plnBalance(),
                0));
        return AccountDto.fromAccount(account);
    }

    @Transactional
    public void deleteAccount(String pesel) {
        Account account = accountRepository.findByPesel(pesel)
                .orElseThrow(() -> new AccountNotFoundException(pesel));
        accountRepository.delete(account);
    }

    @Transactional
    public void processExchangeRequest(ExchangeRequestDto exchangeRequestDto) {
        Account account = accountRepository.findByPesel(exchangeRequestDto.getPesel())
                .orElseThrow(() -> new AccountNotFoundException(exchangeRequestDto.getPesel()));
        boolean exchangeResult = account.exchange(exchangeRequestDto.getCurrencyFrom(), exchangeRequestDto.getCurrencyTo(), exchangeRequestDto.getAmountFrom(), exchangeRequestDto.getAmountTo());
        ExchangeStatus exchangeStatus = exchangeResult ? ExchangeStatus.COMPLETED : ExchangeStatus.REJECTED;
        exchangeResponseProducer.postExchangeResponseDtoToQueue(new ExchangeResponseDto(exchangeRequestDto.getRequestId(), exchangeStatus));
        accountRepository.save(account);
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
