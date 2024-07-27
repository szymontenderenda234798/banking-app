package pl.kurs.java.account.model.command;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.NonNull;
import pl.kurs.java.account.validate.annotation.IsAdult;
import pl.kurs.java.account.validate.annotation.ValidPesel;

import java.math.BigDecimal;

public record CreateAccountCommand(
        @ValidPesel @IsAdult @NonNull
        String pesel,

        @NonNull @Pattern(regexp = "^[a-zA-Z]+$", message = "INVALID_NAME_FORMAT_ONLY_LETTERS_ALLOWED")
        String name,

        @NonNull @Pattern(regexp = "^[a-zA-Z]+$", message = "INVALID_SURNAME_FORMAT_ONLY_LETTERS_ALLOWED")
        String surname,

        @PositiveOrZero(message = "INVALID_BALANCE_NEGATIVE_VALUE")
        BigDecimal startingBalance) {
}