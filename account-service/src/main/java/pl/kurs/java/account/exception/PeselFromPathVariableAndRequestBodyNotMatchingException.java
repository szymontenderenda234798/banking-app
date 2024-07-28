package pl.kurs.java.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class PeselFromPathVariableAndRequestBodyNotMatchingException extends RuntimeException {
    public PeselFromPathVariableAndRequestBodyNotMatchingException(String pathVariablePesel, String requestBodyPesel) {
        super("PESEL from PathVariable " + pathVariablePesel + " does not match the PESEL from request body " + requestBodyPesel);
    }
}
