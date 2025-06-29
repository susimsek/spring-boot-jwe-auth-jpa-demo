package io.github.susimsek.springbootjweauthjpademo.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final ProblemType problemType;
    private final String field;
    private final transient Object rejectedValue;

    public ResourceNotFoundException(ProblemType problemType, String field, Object rejectedValue) {
        super(problemType.getError());
        this.problemType = problemType;
        this.field = field;
        this.rejectedValue = rejectedValue;
    }
}
