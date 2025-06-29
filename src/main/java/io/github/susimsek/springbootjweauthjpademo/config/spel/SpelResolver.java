package io.github.susimsek.springbootjweauthjpademo.config.spel;

import java.lang.reflect.Method;

public interface SpelResolver {

    String resolve(Method method, Object[] arguments, String spelExpression);
}
