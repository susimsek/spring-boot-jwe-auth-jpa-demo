package io.github.susimsek.springbootjweauthjpademo.config.resolver;

import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

public class QrCodeSizeArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String PARAM = "size";
    private static final String DEFAULT = "200x200";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(QrCodeSize.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        String text = webRequest.getParameter(PARAM);
        if (!StringUtils.hasText(text)) {
            text = DEFAULT;
        }
        return QrCodeSize.parse(text);
    }
}
