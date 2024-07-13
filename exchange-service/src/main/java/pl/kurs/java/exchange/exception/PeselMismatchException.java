package pl.kurs.java.exchange.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PeselMismatchException extends RuntimeException {
    public PeselMismatchException(String message) {
        super(message);
    }
}
