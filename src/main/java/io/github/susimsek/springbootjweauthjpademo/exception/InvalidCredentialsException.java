package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.Getter;

@Getter
public class InvalidCredentialsException extends RuntimeException {
    private final ProblemType problemType;

    public InvalidCredentialsException(ProblemType problemType) {
        super(problemType.getError());
        this.problemType = problemType;
    }
}
