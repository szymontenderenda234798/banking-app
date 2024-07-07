package pl.kurs.java.account.model.command;

import jakarta.validation.constraints.Pattern;
import lombok.NonNull;
import pl.kurs.java.account.validate.annotation.IsAdult;
import pl.kurs.java.account.validate.annotation.ValidPesel;

public record UpdateAccountCommand(
        @NonNull
        String currentPesel,

        @NonNull @ValidPesel @IsAdult
        String newPesel,

        @NonNull @Pattern(regexp = "^[a-zA-Z]+$", message = "Name must contain only letters")
        String newName,

        @NonNull @Pattern(regexp = "^[a-zA-Z]+$", message = "Surname must contain only letters")
        String newSurname) {
}
