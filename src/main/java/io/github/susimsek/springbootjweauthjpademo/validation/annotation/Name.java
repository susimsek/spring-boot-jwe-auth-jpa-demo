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
@Constraint(validatedBy = {}) // uses built-in PatternValidator
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Pattern(
    regexp = "^(?>[A-Za-zÇĞİÖŞÜçğıöşü]+)(?:[ \\-](?>[A-Za-zÇĞİÖŞÜçğıöşü]+))*$"
)
@ReportAsSingleViolation
public @interface Name {
    String message() default "{jakarta.validation.constraints.Name.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
