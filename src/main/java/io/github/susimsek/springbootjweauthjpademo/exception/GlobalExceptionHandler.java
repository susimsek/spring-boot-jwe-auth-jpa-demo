package io.github.susimsek.springbootjweauthjpademo.exception;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.ui.Model;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ProblemResponseBuilder builder;

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
        @NonNull HttpMediaTypeNotAcceptableException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("HttpMediaTypeNotAcceptable: supported={}", ex.getSupportedMediaTypes());
        if (!ex.getSupportedMediaTypes().isEmpty()) {
            headers.setAccept(ex.getSupportedMediaTypes());
        }
        return builder.build(
            ProblemType.NOT_ACCEPTABLE,
            HttpStatus.NOT_ACCEPTABLE,
            request,
            headers,
            Collections.emptyMap(),
            ex.getSupportedMediaTypes()
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
        @NonNull HttpMediaTypeNotSupportedException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("HttpMediaTypeNotSupported: contentType={}, supported={}",
            ex.getContentType(), ex.getSupportedMediaTypes());
        headers.setAccept(ex.getSupportedMediaTypes());
        return builder.build(
            ProblemType.UNSUPPORTED_MEDIA_TYPE,
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            request,
            headers,
            Collections.emptyMap(),
            ex.getContentType(), ex.getSupportedMediaTypes()
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
        @NonNull HttpRequestMethodNotSupportedException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("HttpRequestMethodNotSupported: method={}, supported={}",
            ex.getMethod(), ex.getSupportedHttpMethods());
        if (ex.getSupportedHttpMethods() != null) {
            headers.setAllow(ex.getSupportedHttpMethods());
        }
        return builder.build(
            ProblemType.METHOD_NOT_ALLOWED,
            HttpStatus.METHOD_NOT_ALLOWED,
            request,
            headers,
            Collections.emptyMap(),
            ex.getMethod()
        );
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
        @NonNull org.springframework.http.converter.HttpMessageNotReadableException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("HttpMessageNotReadable: {}", ex.getMostSpecificCause().getMessage());
        return builder.build(
            ProblemType.MESSAGE_NOT_READABLE,
            HttpStatus.BAD_REQUEST,
            request,
            headers,
            Collections.emptyMap()
        );
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(
        @NonNull org.springframework.beans.TypeMismatchException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("TypeMismatch: property={}, value={}, requiredType={}",
            ex.getPropertyName(), ex.getValue(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return builder.build(
            ProblemType.TYPE_MISMATCH,
            HttpStatus.BAD_REQUEST,
            request,
            headers,
            Collections.emptyMap(),
            ex.getPropertyName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
            ex.getValue()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
        @NonNull MissingServletRequestParameterException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("MissingServletRequestParameter: parameter={}, expectedType={}",
            ex.getParameterName(), ex.getParameterType());
        return builder.build(
            ProblemType.MISSING_REQUEST_PARAMETER,
            HttpStatus.BAD_REQUEST,
            request,
            headers,
            Collections.emptyMap(),
            ex.getParameterName(), ex.getParameterType()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
        @NonNull MissingServletRequestPartException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("MissingServletRequestPart: partName={}", ex.getRequestPartName());
        return builder.build(
            ProblemType.MISSING_REQUEST_PART,
            HttpStatus.BAD_REQUEST,
            request,
            headers,
            Collections.emptyMap(),
            ex.getRequestPartName()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Object> handleUnauthorized(
        AuthenticationException ex,
        @NonNull WebRequest request
    ) {
        log.debug("AuthenticationException: {}", ex.getMessage());
        return builder.build(
            ProblemType.INVALID_TOKEN,
            HttpStatus.UNAUTHORIZED,
            request,
            null,
            Collections.emptyMap()
        );
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Object> handleJwtException(
        JwtException ex,
        @NonNull WebRequest request
    ) {
        log.debug("JwtException: {}", ex.getMessage());
        return builder.build(
            ProblemType.INVALID_TOKEN,
            HttpStatus.UNAUTHORIZED,
            request,
            null,
            Collections.emptyMap()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleForbidden(
        AccessDeniedException ex,
        @NonNull WebRequest request
    ) {
        log.debug("AccessDeniedException: {}", ex.getMessage());
        return builder.build(
            ProblemType.ACCESS_DENIED,
            HttpStatus.FORBIDDEN,
            request,
            null,
            Collections.emptyMap()
        );
    }

    @ExceptionHandler(value = NoResourceFoundException.class, produces = MediaType.TEXT_HTML_VALUE)
    public String handleNotFoundHtml(
        NoResourceFoundException ex,
        Model model,
        HttpServletResponse response
    ) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    @ExceptionHandler(MfaSetupRequiredException.class)
    public ResponseEntity<Object> handleMfaSetupRequired(
        MfaSetupRequiredException ex,
        @NonNull WebRequest request
    ) {
        log.debug("MfaSetupRequiredException: {}", ex.getMessage());
        return builder.build(
            ProblemType.MFA_SETUP_REQUIRED,
            HttpStatus.FORBIDDEN,
            request,
            null,
            Collections.emptyMap()
        );
    }

    @ExceptionHandler(InvalidOtpException.class)
    public ResponseEntity<Object> handleInvalidOtp(
        InvalidOtpException ex,
        @NonNull WebRequest request
    ) {
        log.debug("InvalidOtpException: {}", ex.getMessage());
        return builder.build(
            ProblemType.INVALID_OTP,
            HttpStatus.UNAUTHORIZED,
            request,
            null,
            Collections.emptyMap()
        );
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<Object> handleResourceConflict(
        ResourceConflictException ex,
        @NonNull WebRequest request
    ) {
        log.debug("ResourceConflictException: field={}, rejectedValue={}, message={}",
            ex.getField(), ex.getRejectedValue(), ex.getMessage());
        return builder.build(
            ex.getProblemType(),
            HttpStatus.CONFLICT,
            request,
            null,
            ex.getField(), ex.getRejectedValue()
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFound(
        ResourceNotFoundException ex,
        @NonNull WebRequest request
    ) {
        log.debug("ResourceNotFoundException: field={}, rejectedValue={}, message={}",
            ex.getField(), ex.getRejectedValue(), ex.getMessage());
        return builder.build(
            ex.getProblemType(),
            HttpStatus.NOT_FOUND,
            request,
            null,
            ex.getField(), ex.getRejectedValue()
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Object> handleValidation(
        ValidationException ex,
        @NonNull WebRequest request
    ) {
        log.debug("ValidationException: rejectedValue={}, message={}",
            ex.getRejectedValue(), ex.getMessage());
        return builder.build(
            ex.getProblemType(),
            HttpStatus.BAD_REQUEST,
            request,
            null,
            Collections.emptyMap(),
            ex.getRejectedValue()
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        @NonNull MethodArgumentNotValidException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("MethodArgumentNotValidException: {}", ex.getMessage());
        List<Violation> violations = Stream.concat(
            ex.getFieldErrors().stream().map(Violation::new),
            ex.getGlobalErrors().stream().map(Violation::new)
        ).toList();
        return builder.build(
            ProblemType.VALIDATION_FAILED,
            HttpStatus.BAD_REQUEST,
            request,
            headers,
            violations
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(
        ConstraintViolationException ex,
        @NonNull WebRequest request
    ) {
        log.debug("ConstraintViolationException: {}", ex.getMessage());
        List<Violation> violations = ex.getConstraintViolations()
            .stream()
            .map(Violation::new)
            .toList();
        return builder.build(
            ProblemType.CONSTRAINT_VIOLATION,
            HttpStatus.BAD_REQUEST,
            request,
            null,
            violations
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleMethodArgumentTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        @NonNull WebRequest request
    ) {
        log.debug("MethodArgumentTypeMismatchException: param={}, value={}", ex.getName(), ex.getValue());
        return builder.build(
            ProblemType.TYPE_MISMATCH,
            HttpStatus.BAD_REQUEST,
            request,
            null,
            Collections.emptyMap(),
            ex.getName(),
            ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown",
            ex.getValue()
        );
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
        @NonNull NoHandlerFoundException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("NoHandlerFound: method={}, path={}", ex.getHttpMethod(), ex.getRequestURL());
        return builder.build(
            ProblemType.NOT_FOUND,
            HttpStatus.NOT_FOUND,
            request,
            headers,
            Collections.emptyMap()
        );
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<Object> handleInvalidToken(
        InvalidTokenException ex,
        @NonNull WebRequest request
    ) {
        log.debug("InvalidTokenException: {}", ex.getMessage());
        return builder.build(
            ex.getProblemType(),
            HttpStatus.BAD_REQUEST,
            request,
            null,
            Collections.emptyMap()
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Object> handleInvalidCredentials(
        InvalidCredentialsException ex,
        @NonNull WebRequest request
    ) {
        log.debug("InvalidCredentialsException: {}", ex.getMessage());
        return builder.build(
            ProblemType.INVALID_CREDENTIALS,
            HttpStatus.UNAUTHORIZED,
            request,
            null,
            Collections.emptyMap()
        );
    }

    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<Object> handleAccountLocked(
        AccountLockedException ex,
        @NonNull WebRequest request
    ) {
        log.debug("AccountLockedException: {}", ex.getMessage());
        // Pass lock expiration in properties map
        Map<String, Object> properties = Collections.singletonMap(
            ErrorConstants.EXPIRES_AT_KEY, ex.getLockExpiresAt()
        );
        return builder.build(
            ProblemType.ACCOUNT_LOCKED,
            HttpStatus.LOCKED,
            request,
            null,
            properties
        );
    }

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(
        @NonNull MaxUploadSizeExceededException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("MaxUploadSizeExceededException: {}", ex.getMessage());
        return builder.build(
            ProblemType.FILE_TOO_LARGE,
            HttpStatus.PAYLOAD_TOO_LARGE,
            request,
            headers,
            Collections.emptyMap()
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Object> handleRateLimitExceeded(
        RateLimitExceededException ex,
        @NonNull WebRequest request
    ) {
        log.debug("RateLimitExceededException: {}", ex.getMessage());
        HttpHeaders headers = new HttpHeaders();
        headers.add(org.springframework.http.HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()));
        headers.add("X-Rate-Limit-Limit", String.valueOf(ex.getLimit()));
        headers.add("X-Rate-Limit-Remaining", String.valueOf(ex.getRemaining()));
        headers.add("X-Rate-Limit-Reset", String.valueOf(Instant.now().getEpochSecond() + ex.getRetryAfterSeconds()));
        return builder.build(
            ProblemType.RATE_LIMIT_EXCEEDED,
            HttpStatus.TOO_MANY_REQUESTS,
            request,
            headers,
            Collections.emptyMap()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(
        Exception ex,
        @NonNull WebRequest request
    ) {
        log.error("Unhandled exception: ", ex);
        return builder.build(
            ProblemType.SERVER_ERROR,
            HttpStatus.INTERNAL_SERVER_ERROR,
            request,
            null,
           Collections.emptyMap()
        );
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(
        @NonNull AsyncRequestTimeoutException ex,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        log.debug("AsyncRequestTimeoutException: {}", ex.getMessage());
        return builder.build(
            ProblemType.GATEWAY_TIMEOUT,
            HttpStatus.GATEWAY_TIMEOUT,
            request,
           null,
            Collections.emptyMap()
        );
    }
}
