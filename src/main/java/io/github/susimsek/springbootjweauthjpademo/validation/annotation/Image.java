package io.github.susimsek.springbootjweauthjpademo.validation.annotation;

import io.github.susimsek.springbootjweauthjpademo.validation.ImageValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = ImageValidator.class)
@Target({ ElementType.PARAMETER, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Image {
    String message() default "{jakarta.validation.constraints.Image.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    String[] allowedTypes() default {
        "image/png", "image/jpeg", "image/jpg", "image/webp" };
}
