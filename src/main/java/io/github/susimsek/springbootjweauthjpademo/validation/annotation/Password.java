package io.github.susimsek.springbootjweauthjpademo.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {}) // Uses built-in PatternValidator
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Pattern(
    regexp = "^(?=.*[A-ZÇĞİÖŞÜ])(?=.*[a-zçğıöşü])(?=.*\\d).+$",
    message = "{jakarta.validation.constraints.Password.message}"
)
@ReportAsSingleViolation
public @interface Password {
    String message() default "{jakarta.validation.constraints.Password.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
