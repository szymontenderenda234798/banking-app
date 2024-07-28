package pl.kurs.java.account.model.transaction.request;

import pl.kurs.java.account.model.transaction.enums.TransactionStatus;

import java.math.BigDecimal;

public record BuyTransactionRequest(String pesel, String currencyToBuy, BigDecimal amountToBuy, BigDecimal rate,
                                    String currencyToSpend, BigDecimal amountToSpend, TransactionStatus transactionStatus) {
}
