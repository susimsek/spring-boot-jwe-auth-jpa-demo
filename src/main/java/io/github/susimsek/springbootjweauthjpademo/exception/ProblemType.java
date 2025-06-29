package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.Getter;

import java.net.URI;

import static io.github.susimsek.springbootjweauthjpademo.exception.ErrorConstants.PROBLEM_BASE_URL;

public enum ProblemType {

    NOT_ACCEPTABLE("problem.not_acceptable", "not_acceptable"),
    UNSUPPORTED_MEDIA_TYPE("problem.unsupported_media_type", "unsupported_media_type"),
    METHOD_NOT_ALLOWED("problem.method_not_allowed", "method_not_allowed"),
    MESSAGE_NOT_READABLE("problem.message_not_readable", "message_not_readable"),
    TYPE_MISMATCH("problem.type_mismatch", "type_mismatch"),
    MISSING_REQUEST_PARAMETER("problem.missing_request_parameter", "missing_request_parameter"),
    MISSING_REQUEST_PART("problem.missing_request_part", "missing_request_part"),
    INVALID_TOKEN("problem.invalid_token", "invalid_token"),
    ACCESS_DENIED("problem.access_denied", "access_denied"),
    MFA_SETUP_REQUIRED("problem.mfa_setup_required", "mfa_setup_required"),
    INVALID_OTP("problem.invalid_otp", "invalid_otp"),
    ACCOUNT_LOCKED("problem.account_locked", "account_locked"),
    RESOURCE_CONFLICT("problem.resource_conflict", "resource_conflict"),
    RESOURCE_NOT_FOUND("problem.resource_not_found", "resource_not_found"),
    INVALID_REQUEST("problem.invalid_request", "invalid_request"),
    VALIDATION_FAILED("problem.validation_failed", "validation_failed"),
    CONSTRAINT_VIOLATION("problem.constraint_violation", "constraint_violation"),
    NOT_FOUND("problem.not_found", "not_found"),
    INVALID_CREDENTIALS("problem.invalid_credentials", "invalid_credentials"),
    FILE_TOO_LARGE("problem.file_too_large", "file_too_large"),
    RATE_LIMIT_EXCEEDED("problem.rate_limit_exceeded", "rate_limit_exceeded"),
    SERVER_ERROR("problem.server_error", "server_error"),
    GATEWAY_TIMEOUT("problem.gateway_timeout", "gateway_timeout"),
    USER_NOT_FOUND("problem.user-not-found", "user_not_found"),
    INVALID_PASSWORD("problem.invalid_password", "invalid_password"),
    INVALID_VERIFICATION_TOKEN("problem.invalid_verification_token", "invalid_verification_token"),
    VERIFICATION_LINK_EXPIRED("problem.verification_link_expired", "verification_link_expired"),
    RESET_TOKEN_INVALID("problem.reset_token_invalid", "reset_token_invalid"),
    RESET_LINK_EXPIRED("problem.reset_link_expired", "reset_link_expired"),
    INVALID_AUTHORITY("problem.invalid_authority", "invalid_authority"),
    PASSWORD_CANNOT_REUSE("problem.password_cannot_reuse", "password_cannot_reuse"),
    EMAIL_CANNOT_REUSE("problem.email_cannot_reuse", "email_cannot_reuse"),
    QRCODE_SIZE_INVALID_FORMAT("problem.qrcode_size.invalid_format", "qrcode_size_invalid_format"),
    AUTHORITY_NOT_FOUND("problem.authority_not_found", "authority_not_found"),
    IMAGE_NOT_FOUND("problem.image_not_found", "image_not_found"),
    AUTHORITY_NAME_CONFLICT("problem.authority_name_conflict", "authority_name_conflict"),
    AUTHORITY_PROTECTED("problem.authority_protected", "authority_protected"),
    USER_PROTECTED("problem.user_protected", "user_protected"),
    AUTHORITY_ASSIGNED_CONFLICT("problem.authority_assigned_conflict", "authority_assigned_conflict"),
    USERNAME_CONFLICT("problem.username_conflict", "username_conflict"),
    EMAIL_CONFLICT("problem.email_conflict", "email_conflict"),
    NO_PENDING_EMAIL_CHANGE("problem.no_pending_email_change", "no_pending_email_change"),
    MFA_NOT_ENABLED("problem.mfa_not_enabled", "mfa_not_enabled"),
    MFA_ALREADY_VERIFIED("problem.mfa_already_verified", "mfa_already_verified"),
    MFA_ALREADY_DISABLED("problem.mfa_already_disabled", "mfa_already_disabled"),
    SELF_DELETION_NOT_ALLOWED("problem.self_deletion_not_allowed", "self_deletion_not_allowed"),
    EMAIL_ALREADY_VERIFIED("problem.email_already_verified", "email_already_verified"),
    MESSAGE_NOT_FOUND("problem.message_not_found", "message_not_found"),
    MESSAGE_CODE_CONFLICT("problem.message_code_conflict", "message_code_conflict");

    private final String messagePrefix;
    @Getter
    private final String error;

    ProblemType(String messagePrefix, String error) {
        this.messagePrefix = messagePrefix;
        this.error = error;
    }

    public String getTitleKey() {
        return messagePrefix + ".title";
    }

    public String getDetailKey() {
        return messagePrefix + ".detail";
    }

    public URI getType() {
        return URI.create(PROBLEM_BASE_URL + "/" + name().toLowerCase());
    }
}
