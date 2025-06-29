package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.NoArgsConstructor;
import lombok.AccessLevel;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorConstants {

    public static final String PROBLEM_BASE_URL = "https://example.com/problem";
    public static final String VIOLATION_KEY = "violations";
    public static final String ERROR_KEY = "error";
    public static final String FIELD_KEY = "field";
    public static final String REJECTED_VALUE_KEY = "rejectedValue";
    public static final String EXPIRES_AT_KEY = "expiresAt";
}
