package pl.kurs.java.account.model.dto;

import pl.kurs.java.account.model.Account;

import java.util.List;

public record AccountDto(String pesel, String name, String surname, String accountNumber, List<SubAccountDto> subAccounts) {
    public static AccountDto fromAccount(Account account) {
        return new AccountDto(account.getPesel(), account.getName(), account.getSurname(), account.getAccountNumber(), account.getSubAccounts().stream().map(SubAccountDto::fromSubAccount).toList());
    }
}
