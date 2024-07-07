package pl.kurs.java.account.error;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.kurs.java.account.error.dto.FieldError;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Set<FieldError>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exc) {
        return ResponseEntity.badRequest().body(
                exc.getBindingResult().getFieldErrors().stream()
                        .map(error -> new FieldError(error.getField(), error.getDefaultMessage()))
                        .collect(Collectors.toCollection(TreeSet::new))
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Set<FieldError>> handleConstraintViolationException(ConstraintViolationException exc) {
        return ResponseEntity.badRequest().body(exc.getConstraintViolations().stream()
                .map(violation -> new FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()))
                .collect(Collectors.toCollection(TreeSet::new))
        );
    }
}