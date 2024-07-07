package pl.kurs.java.account.service;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.kurs.java.account.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import pl.kurs.java.account.exception.CurrentPeselNotMatchingException;
import pl.kurs.java.account.model.Account;
import pl.kurs.java.account.model.command.CreateAccountCommand;
import pl.kurs.java.account.model.command.UpdateAccountCommand;
import pl.kurs.java.account.model.dto.AccountDto;
import org.springframework.stereotype.Service;
import pl.kurs.java.account.repository.AccountRepository;

import java.security.SecureRandom;


@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public Page<AccountDto> getAccounts(Pageable pageable) {
        return accountRepository.findAll(pageable)
                .map(AccountDto::fromAccount);
    }

    public AccountDto getAccount(String pesel) {
        return accountRepository.findById(pesel)
                .map(AccountDto::fromAccount)
                .orElseThrow(() -> new AccountNotFoundException(pesel));
    }

    @Transactional
    public AccountDto updateAccount(String currentPesel, UpdateAccountCommand updateAccountCommand) {
        if (!currentPesel.equals(updateAccountCommand.currentPesel())) {
            throw new CurrentPeselNotMatchingException(currentPesel, updateAccountCommand.currentPesel());
        }
        Account account = accountRepository.findById(currentPesel)
                .orElseThrow(() -> new AccountNotFoundException(currentPesel));
        account.setPesel(updateAccountCommand.newPesel());
        account.setName(updateAccountCommand.newName());
        account.setSurname(updateAccountCommand.newSurname());
        Account updatedAccount = accountRepository.save(account);
        return AccountDto.fromAccount(updatedAccount);
    }

    @Transactional
    public AccountDto createAccount(CreateAccountCommand createAccountCommand) {
        Account account = accountRepository.save(new Account(
                createAccountCommand.pesel(),
                createAccountCommand.name(),
                createAccountCommand.surname(),
                generateUniqueAccountNumber(),
                createAccountCommand.plnBalance(),
                0));
        return AccountDto.fromAccount(account);
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

    public void deleteAccount(String pesel) {
        Account account = accountRepository.findById(pesel)
                .orElseThrow(() -> new AccountNotFoundException(pesel));
        accountRepository.delete(account);
    }
}
