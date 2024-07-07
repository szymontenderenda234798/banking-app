package pl.kurs.java.account.model.command;

import jakarta.validation.constraints.Pattern;
import lombok.NonNull;
import pl.kurs.java.account.validate.annotation.IsAdult;
import pl.kurs.java.account.validate.annotation.ValidPesel;

public record CreateAccountCommand(
        @ValidPesel @IsAdult @NonNull
        String pesel,

        @NonNull @Pattern(regexp = "^[a-zA-Z]+$", message = "Name must contain only letters")
        String name,

        @NonNull @Pattern(regexp = "^[a-zA-Z]+$", message = "Surname must contain only letters")
        String surname,

        double plnBalance) {
}
