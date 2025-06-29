package io.github.susimsek.springbootjweauthjpademo.config.spel;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Method;

@Getter
@RequiredArgsConstructor
public class SpelRootObject {
    private final String className;
    private final String methodName;
    private final Object[] args;

    public SpelRootObject(Method method, Object[] args) {
        this(method.getDeclaringClass().getName(), method.getName(), args);
    }
}
