package pl.kurs.java.account.model.dto;

import pl.kurs.java.account.model.Account;

public record AccountDto(String pesel, String name, String surname, String accountNumber, double plnBalance,
                         double usdBalance) {
    public static AccountDto fromAccount(Account account) {
        return new AccountDto(account.getPesel(), account.getName(), account.getSurname(), account.getAccountNumber(), account.getPlnBalance(), account.getUsdBalance());
    }
}
