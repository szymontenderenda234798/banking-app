package pl.kurs.java.account.validate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import pl.kurs.java.account.service.provider.DateProvider;
import pl.kurs.java.account.validate.annotation.IsAdult;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@RequiredArgsConstructor
public class AdultValidator implements ConstraintValidator<IsAdult, String> {

    private final DateProvider dateProvider;

    @Override
    public void initialize(IsAdult constraintAnnotation) {}

    @Override
    public boolean isValid(String pesel, ConstraintValidatorContext context) {
        LocalDate birthDate = extractBirthDate(pesel);
        LocalDate now = dateProvider.provideNow().toLocalDate();
        return Period.between(birthDate, now).getYears() >= 18;
    }

    public static LocalDate extractBirthDate(String pesel) {
        int year = Integer.parseInt(pesel.substring(0, 2));
        int month = Integer.parseInt(pesel.substring(2, 4));
        int day = Integer.parseInt(pesel.substring(4, 6));

        if (month >= 81 && month <= 92) {
            year += 1800;
            month -= 80;
        } else if (month >= 1 && month <= 12) {
            year += 1900;
        } else if (month >= 21 && month <= 32) {
            year += 2000;
            month -= 20;
        } else if (month >= 41 && month <= 52) {
            year += 2100;
            month -= 40;
        } else if (month >= 61 && month <= 72) {
            year += 2200;
            month -= 60;
        } else {
            throw new IllegalArgumentException("Invalid birth month in PESEL");
        }

        String dateString = String.format("%04d-%02d-%02d", year, month, day);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        try {
            return LocalDate.parse(dateString, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid birth date in PESEL");
        }
    }
}