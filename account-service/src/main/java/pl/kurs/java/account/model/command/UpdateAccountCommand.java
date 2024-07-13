package pl.kurs.java.account.model.command;

import jakarta.validation.constraints.Pattern;
import lombok.NonNull;
import pl.kurs.java.account.validate.annotation.IsAdult;
import pl.kurs.java.account.validate.annotation.ValidPesel;

public record UpdateAccountCommand(
        @NonNull
        String currentPesel,

        @NonNull @Pattern(regexp = "^[a-zA-Z]+$", message = "INVALID_NAME_FORMAT_ONLY_LETTERS_ALLOWED")
        String newName,

        @NonNull @Pattern(regexp = "^[a-zA-Z]+$", message = "INVALID_SURNAME_FORMAT_ONLY_LETTERS_ALLOWED")
        String newSurname) {
}