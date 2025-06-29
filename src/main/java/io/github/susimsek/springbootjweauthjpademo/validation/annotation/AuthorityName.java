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
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Pattern(
    regexp = "^ROLE_[A-Z_]+$"
)
@ReportAsSingleViolation
public @interface AuthorityName {
    String message() default "{jakarta.validation.constraints.AuthorityName.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
