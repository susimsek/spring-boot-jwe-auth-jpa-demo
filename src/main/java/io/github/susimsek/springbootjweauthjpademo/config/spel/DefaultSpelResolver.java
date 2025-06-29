package io.github.susimsek.springbootjweauthjpademo.config.spel;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Method;

@Slf4j
@RequiredArgsConstructor
public class DefaultSpelResolver implements EmbeddedValueResolverAware, SpelResolver {
    private static final String PLACEHOLDER_SPEL_REGEX = "^\\$\\{.+}$";
    private static final String METHOD_SPEL_REGEX = "^#.+$";
    private static final String BEAN_SPEL_REGEX = "^@.+";

    private final SpelExpressionParser expressionParser;
    private final ParameterNameDiscoverer parameterNameDiscoverer;
    private final BeanFactory beanFactory;
    private StringValueResolver stringValueResolver;

    @Override
    public String resolve(Method method, Object[] arguments, String spelExpression) {
        if (!StringUtils.hasText(spelExpression)) {
            return spelExpression;
        }
        try {
            if (spelExpression.matches(PLACEHOLDER_SPEL_REGEX) && stringValueResolver != null) {
                return stringValueResolver.resolveStringValue(spelExpression);
            }
            if (spelExpression.matches(METHOD_SPEL_REGEX)) {
                SpelRootObject root = new SpelRootObject(method, arguments);
                MethodBasedEvaluationContext
                    ctx = new MethodBasedEvaluationContext(root, method, arguments, parameterNameDiscoverer);
                return expressionParser.parseExpression(spelExpression).getValue(ctx, String.class);
            }
            if (spelExpression.matches(BEAN_SPEL_REGEX)) {
                SpelRootObject root = new SpelRootObject(method, arguments);
                StandardEvaluationContext ctx = new StandardEvaluationContext(root);
                ctx.setBeanResolver(new BeanFactoryResolver(beanFactory));
                return expressionParser.parseExpression(spelExpression).getValue(ctx, String.class);
            }
        } catch (Exception e) {
            log.warn("SpEL resolution failed for expression {} on method {}", spelExpression, method, e);
        }
        return spelExpression;
    }

    @Override
    public void setEmbeddedValueResolver(@NonNull StringValueResolver resolver) {
        this.stringValueResolver = resolver;
    }
}
