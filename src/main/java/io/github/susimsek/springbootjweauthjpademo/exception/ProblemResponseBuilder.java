package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.WebRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.susimsek.springbootjweauthjpademo.exception.ErrorConstants.ERROR_KEY;
import static io.github.susimsek.springbootjweauthjpademo.exception.ErrorConstants.FIELD_KEY;
import static io.github.susimsek.springbootjweauthjpademo.exception.ErrorConstants.REJECTED_VALUE_KEY;
import static io.github.susimsek.springbootjweauthjpademo.exception.ErrorConstants.VIOLATION_KEY;


@Component
@RequiredArgsConstructor
public class ProblemResponseBuilder {

    private final MessageSource messageSource;

    public ResponseEntity<Object> build(
        ProblemType type,
        HttpStatus status,
        WebRequest request,
        HttpHeaders headers,
        Map<String, Object> properties,
        Object... args
    ) {
        ProblemDetail pd = buildProblemDetail(type, status, request, properties, args);
        // Add custom properties to the ProblemDetail
        return ResponseEntity.status(status).headers(headers).body(pd);
    }

    public ResponseEntity<Object> build(
        ProblemType type,
        HttpStatus status,
        WebRequest request,
        HttpHeaders headers,
        String field,
        Object rejectedValue,
        Object... args
    ) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(FIELD_KEY, field);
        properties.put(REJECTED_VALUE_KEY, rejectedValue);

        ProblemDetail pd = buildProblemDetail(type, status, request, properties, args);
        return ResponseEntity.status(status).headers(headers).body(pd);
    }

    public ResponseEntity<Object> build(
        ProblemType type,
        HttpStatus status,
        WebRequest request,
        HttpHeaders headers,
        List<Violation> violations
    ) {
        Map<String,Object> properties = Collections.singletonMap(VIOLATION_KEY, violations);
        ProblemDetail pd = buildProblemDetail(type, status, request, properties);
        return ResponseEntity.status(status).headers(headers).body(pd);
    }

    private ProblemDetail buildProblemDetail(
        ProblemType type,
        HttpStatus status,
        WebRequest request,
        Map<String, Object> properties,
        Object... args
    ) {
        String detail = messageSource.getMessage(type.getDetailKey(), args, request.getLocale());
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setTitle(messageSource.getMessage(type.getTitleKey(), null, request.getLocale()));
        pd.setType(type.getType());
        pd.setProperty(ERROR_KEY, type.getError());
        if (!CollectionUtils.isEmpty(properties)) {
            properties.forEach(pd::setProperty);
        }
        return pd;
    }
}
