package pl.kurs.java.account.model.dto;

import pl.kurs.java.account.model.Account;

import java.math.BigDecimal;
import java.util.List;

public record AccountDto(String pesel, String name, String surname, BigDecimal balance, String currency,
                         String accountNumber, List<SubAccountDto> subAccounts) {
    public static AccountDto fromAccount(Account account) {
        return new AccountDto(account.getPesel(), account.getName(), account.getSurname(), account.getBalance(), account.getCurrency(), account.getAccountNumber(), account.getSubAccounts().stream().map(SubAccountDto::fromSubAccount).toList());
    }
}
