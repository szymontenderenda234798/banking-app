package pl.kurs.java.account.model.account.command;

import pl.kurs.java.account.validate.annotation.SupportedForeignCurrency;

public record CreateSubAccountCommand(@SupportedForeignCurrency String currency) {
}
