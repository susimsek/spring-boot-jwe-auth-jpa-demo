package io.github.susimsek.springbootjweauthjpademo.validation;

import io.github.susimsek.springbootjweauthjpademo.validation.annotation.NotEmptyFile;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class NotEmptyFileValidator
    implements ConstraintValidator<NotEmptyFile, MultipartFile> {

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext ctx) {
        return file != null && !file.isEmpty();
    }
}
