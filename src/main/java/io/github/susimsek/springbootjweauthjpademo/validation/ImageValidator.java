package io.github.susimsek.springbootjweauthjpademo.validation;

import io.github.susimsek.springbootjweauthjpademo.validation.annotation.Image;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ImageValidator implements ConstraintValidator<Image, MultipartFile> {

    private Set<String> allowedTypes;

    @Override
    public void initialize(Image annotation) {
        allowedTypes = Arrays.stream(annotation.allowedTypes())
            .map(String::toLowerCase)
            .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true; // @NotEmptyFile handles emptiness
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            // Build dynamic error message listing allowed types
            String typesList = String.join(", ", allowedTypes);
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                "Invalid image file type. Allowed types: " + typesList
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
