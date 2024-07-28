package pl.kurs.java.account.model.dto;

import lombok.Value;
import pl.kurs.java.account.model.SubAccount;

import java.math.BigDecimal;

public record SubAccountDto(BigDecimal balance, String currency, String accountNumber) {
    public static SubAccountDto fromSubAccount(SubAccount subAccount) {
        return new SubAccountDto(subAccount.getBalance(), subAccount.getCurrency(), subAccount.getAccountNumber());
    }
}
