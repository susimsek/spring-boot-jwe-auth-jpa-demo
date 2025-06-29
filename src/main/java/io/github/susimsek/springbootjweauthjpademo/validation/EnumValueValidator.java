package io.github.susimsek.springbootjweauthjpademo.validation;

import io.github.susimsek.springbootjweauthjpademo.validation.annotation.EnumValue;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {
    private Set<String> acceptedValues;
    private String messageTemplate;

    @Override
    public void initialize(EnumValue annotation) {
        acceptedValues = Arrays.stream(annotation.enumClass().getEnumConstants())
            .map(Enum::name)
            .collect(Collectors.toSet());
        messageTemplate = annotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (acceptedValues.contains(value)) {
            return true;
        }
        String allowed = String.join(", ", acceptedValues);
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
            messageTemplate.replace("{value}", value)
                           .replace("{allowedValues}", allowed)
        ).addConstraintViolation();
        return false;
    }
}
