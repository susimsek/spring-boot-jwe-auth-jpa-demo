package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.Getter;


@Getter
public class RateLimitExceededException extends RuntimeException {

    private final String bucketName;
    private final long limit;
    private final long remaining;
    private final long retryAfterSeconds;

    public RateLimitExceededException(String bucketName, long limit, long remaining, long retryAfterSeconds) {
        super("Rate limit exceeded for bucket: " + bucketName);
        this.bucketName = bucketName;
        this.limit = limit;
        this.remaining = remaining;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
