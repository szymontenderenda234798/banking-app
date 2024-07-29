package pl.kurs.java.account.model.account.dto;

import pl.kurs.java.account.model.account.SubAccount;

import java.math.BigDecimal;

public record SubAccountDto(BigDecimal balance, String currency, String accountNumber) {
    public static SubAccountDto fromSubAccount(SubAccount subAccount) {
        return new SubAccountDto(subAccount.getBalance(), subAccount.getCurrency(), subAccount.getAccountNumber());
    }
}
