package io.github.susimsek.springbootjweauthjpademo.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;
import jakarta.validation.constraints.Pattern;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {}) // uses built-in PatternValidator
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Pattern(
    regexp = "^[a-zA-Z0-9_\\-]+$"
)
@ReportAsSingleViolation
public @interface Username {
    String message() default "{jakarta.validation.constraints.Username.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
