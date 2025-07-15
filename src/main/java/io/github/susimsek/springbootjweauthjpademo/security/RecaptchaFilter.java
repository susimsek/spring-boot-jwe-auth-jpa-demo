package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.exception.InvalidRecaptchaException;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemSupport;
import io.github.susimsek.springbootjweauthjpademo.util.WebUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@RequiredArgsConstructor
public class RecaptchaFilter extends OncePerRequestFilter {

    private final RecaptchaService recaptchaService;
    private final ProblemSupport problemSupport;
    private final RecaptchaTokenResolver tokenResolver;
    private final AntPathMatcher matcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        boolean isRegister = matcher.match("/api/v*/auth/register", uri);
        boolean isLogin    = matcher.match("/api/v*/auth/login", uri);
        return !(isRegister || isLogin);
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
}
