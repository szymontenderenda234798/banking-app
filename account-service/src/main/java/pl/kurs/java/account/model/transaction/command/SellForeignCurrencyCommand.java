package pl.kurs.java.account.model.transaction.command;

import java.math.BigDecimal;

public record SellForeignCurrencyCommand(String pesel, BigDecimal amountToSell, String currency) {
}