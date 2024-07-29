package pl.kurs.java.account.validate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kurs.java.account.validate.annotation.SupportedBaseCurrency;
import pl.kurs.java.account.validate.annotation.SupportedForeignCurrency;
import pl.kurs.java.exchange.config.SupportedCurrenciesConfig;

@Component
@RequiredArgsConstructor
public class ForeignCurrencyValidator implements ConstraintValidator<SupportedForeignCurrency, String> {

    private final SupportedCurrenciesConfig supportedCurrenciesConfig;

    @Override
    public void initialize(SupportedForeignCurrency constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return supportedCurrenciesConfig.isForeignCurrencySupported(s);
    }
}