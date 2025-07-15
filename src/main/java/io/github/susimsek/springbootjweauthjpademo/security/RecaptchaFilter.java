package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.exception.InvalidRecaptchaException;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemSupport;
import io.github.susimsek.springbootjweauthjpademo.util.WebUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
// import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
public class RecaptchaFilter extends OncePerRequestFilter {

    private final RecaptchaService recaptchaService;
    private final ProblemSupport problemSupport;
    private final RecaptchaTokenResolver tokenResolver;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        List<RequestMatcher> matchers = requestMatchers(
            "/api/v*/auth/register",
            "/api/v*/auth/login",
            "/api/v*/auth/forgot-password"
        );
        boolean matches = matchers.stream()
            .anyMatch(matcher -> matcher.matches(request));
        return !matches;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = tokenResolver.resolve(request);
            String ip = WebUtils.getClientIp(request);
            recaptchaService.validate(token, ip);
            filterChain.doFilter(request, response);
        } catch (InvalidRecaptchaException ex) {
            problemSupport.handle(request, response, ex);
        }
    }

    private List<RequestMatcher> requestMatchers(String... patterns) {
        List<RequestMatcher> matchers = new ArrayList<>();
        for (String pattern : patterns) {
            matchers.add(
                PathPatternRequestMatcher.withDefaults().matcher(pattern)
            );
        }
        return matchers;
    }
}
