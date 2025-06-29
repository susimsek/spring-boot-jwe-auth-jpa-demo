package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final ProblemType problemType;
    private final transient Object rejectedValue;

    public ValidationException(ProblemType problemType, Object rejectedValue) {
        super(problemType.getError());
        this.problemType = problemType;
        this.rejectedValue = rejectedValue;
    }
}
