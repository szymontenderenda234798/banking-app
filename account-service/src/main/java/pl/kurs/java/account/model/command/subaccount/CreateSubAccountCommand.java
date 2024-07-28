package pl.kurs.java.account.model.command.subaccount;

import pl.kurs.java.account.validate.annotation.SupportedForeignCurrency;

public record CreateSubAccountCommand(@SupportedForeignCurrency String currency) {
}
