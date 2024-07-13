package pl.kurs.java.exchange.model.command;

public record CreateExchangeRequestCommand(String pesel, String currencyFrom, String currencyTo, double amount) {
}
