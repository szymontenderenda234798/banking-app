package pl.kurs.java.account.model.transaction.command;

import java.math.BigDecimal;

public record BuyForeignCurrencyCommand(String pesel, BigDecimal amountToBuy, String currency) {
}
