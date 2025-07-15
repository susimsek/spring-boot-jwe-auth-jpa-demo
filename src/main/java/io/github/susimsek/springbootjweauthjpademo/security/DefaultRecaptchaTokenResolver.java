package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.exception.InvalidRecaptchaException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DefaultRecaptchaTokenResolver implements RecaptchaTokenResolver {

    private static final String HEADER_NAME = "X-Recaptcha-Token";
    private static final Pattern TOKEN_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");

    @Override
    public String resolve(HttpServletRequest request) {
        String token = request.getHeader(HEADER_NAME);
        if (!StringUtils.hasText(token)) {
            throw new InvalidRecaptchaException();
        }
        Matcher matcher = TOKEN_PATTERN.matcher(token);
        if (!matcher.matches()) {
            throw new InvalidRecaptchaException();
        }
        return token;
    }
}
