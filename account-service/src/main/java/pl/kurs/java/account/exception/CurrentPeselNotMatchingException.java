package pl.kurs.java.account.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class CurrentPeselNotMatchingException extends RuntimeException {
    public CurrentPeselNotMatchingException(String pathVariableCurrentPesel, String updateAccountCommandCurrentPesel) {
        super("Current PESEL from PathVariable " + pathVariableCurrentPesel + " does not match the current PESEL from UpdateAccountCommand " + updateAccountCommandCurrentPesel);
    }
}
