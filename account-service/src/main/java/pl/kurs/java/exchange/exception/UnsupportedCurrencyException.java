package pl.kurs.java.exchange.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class UnsupportedCurrencyException extends RuntimeException{
    public UnsupportedCurrencyException(String message) {
        super(message);
    }
}
