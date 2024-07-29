package pl.kurs.java.account.validate.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.kurs.java.account.validate.annotation.SupportedBaseCurrency;
import pl.kurs.java.exchange.config.SupportedCurrenciesConfig;

@Component
@RequiredArgsConstructor
public class BaseCurrencyValidator implements ConstraintValidator<SupportedBaseCurrency, String> {

    private final SupportedCurrenciesConfig supportedCurrenciesConfig;

    @Override
    public void initialize(SupportedBaseCurrency constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return supportedCurrenciesConfig.isBaseCurrencySupported(s);
    }
}
