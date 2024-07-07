package pl.kurs.java.account.error.dto;

import java.util.Comparator;

public record FieldError(String field, String message) implements Comparable<FieldError> {
    @Override
    public int compareTo(FieldError o) {
        return Comparator.comparing(FieldError::field)
                .thenComparing(FieldError::message)
                .compare(this, o);
    }
}
