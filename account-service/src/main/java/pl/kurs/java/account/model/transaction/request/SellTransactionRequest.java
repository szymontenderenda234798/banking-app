package pl.kurs.java.account.model.transaction.request;

import pl.kurs.java.account.model.transaction.enums.TransactionStatus;

import java.math.BigDecimal;

public record SellTransactionRequest(String pesel, String currencyToSell, BigDecimal amountToSell, BigDecimal rate,
                                    String currencyToReceive, BigDecimal amountToReceive, TransactionStatus transactionStatus) {
}