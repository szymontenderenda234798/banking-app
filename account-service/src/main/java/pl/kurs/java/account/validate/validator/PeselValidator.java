package pl.kurs.java.account.validate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.kurs.java.account.validate.annotation.ValidPesel;

public class PeselValidator implements ConstraintValidator<ValidPesel, String> {

    @Override
    public void initialize(ValidPesel constraintAnnotation) {
    }

    @Override
    public boolean isValid(String pesel, ConstraintValidatorContext context) {
        if (!pesel.matches("\\d{11}")) {
            return false;
        }

        int[] weights = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};
        int sum = 0;

        for (int i = 0; i < 10; i++) {
            int product = weights[i] * Character.getNumericValue(pesel.charAt(i));
            sum += product % 10;
        }

        int controlDigit = Character.getNumericValue(pesel.charAt(10));
        int checksum = (10 - (sum % 10)) % 10;

        return controlDigit == checksum;
    }
}