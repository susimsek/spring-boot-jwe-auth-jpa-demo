package io.github.susimsek.springbootjweauthjpademo.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record Violation(
    @JsonProperty("code")
    String code,

    @JsonProperty("object")
    String objectName,

    @JsonProperty
    String field,

    @JsonProperty
    Object rejectedValue,

    @JsonProperty
    String message
) implements Serializable {


    public Violation(String objectName, String message) {
        this(null, objectName, null, null, message);
    }


    public Violation(FieldError error) {
        this(
            getCode(error.getCode()),
            error.getObjectName().replaceFirst("DTO$", ""),
            error.getField(),
            error.getRejectedValue(),
            error.getDefaultMessage()
        );
    }


    public Violation(ObjectError error) {
        this(
            getCode(error.getCode()),
            null,
            error.getObjectName().replaceFirst("DTO$", ""),
            null,
            error.getDefaultMessage()
        );
    }

    public Violation(ConstraintViolation<?> violation) {
        this(
            getCode(violation.getConstraintDescriptor()
                .getAnnotation().annotationType().getSimpleName()),
            null,
            getField(violation.getPropertyPath()),
            (violation.getInvalidValue() instanceof MultipartFile multipartFile
                ? multipartFile.getOriginalFilename()
                : violation.getInvalidValue()),
            violation.getMessage()
        );
    }


    private static String getField(Path propertyPath) {
        String fieldName = null;
        for (Path.Node node : propertyPath) {
            fieldName = node.getName();
        }
        return fieldName;
    }


    private static String getCode(String annotationName) {
        if (!StringUtils.hasText(annotationName)) {
            return annotationName;
        }
        return annotationName.replaceAll(
            "([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}
