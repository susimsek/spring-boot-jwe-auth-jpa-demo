package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.Getter;
import org.springframework.security.authentication.LockedException;

import java.time.Instant;

@Getter
public class AccountLockedException extends LockedException {

    private final ProblemType problemType;

    private final Instant lockExpiresAt;

    public AccountLockedException(ProblemType problemType, Instant lockExpiresAt) {
        super(problemType.getError());
        this.problemType = problemType;
        this.lockExpiresAt = lockExpiresAt;
    }
}
