package pl.kurs.java.account.validate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import pl.kurs.java.account.validate.validator.PeselValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PeselValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPesel {
    String message() default "Invalid PESEL format";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}