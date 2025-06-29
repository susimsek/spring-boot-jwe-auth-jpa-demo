package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.Getter;

@Getter
public class InvalidTokenException extends RuntimeException {
    private final ProblemType problemType;

    public InvalidTokenException(ProblemType problemType) {
        super(problemType.getError());
        this.problemType = problemType;
    }
}
