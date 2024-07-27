package pl.kurs.java.account.model.dto;

import lombok.Value;
import pl.kurs.java.account.model.SubAccount;

import java.math.BigDecimal;

public record SubAccountDto(String accountNumber, String currency, BigDecimal balance) {
    public static SubAccountDto fromSubAccount(SubAccount subAccount) {
        return new SubAccountDto(subAccount.getAccountNumber(), subAccount.getCurrency(), subAccount.getBalance());
    }
}
