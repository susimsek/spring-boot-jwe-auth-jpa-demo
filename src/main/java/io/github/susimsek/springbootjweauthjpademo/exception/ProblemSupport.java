package io.github.susimsek.springbootjweauthjpademo.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProblemSupport implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final HandlerExceptionResolver handlerExceptionResolver;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) {
        handlerExceptionResolver.resolveException(
            request,
            response,
            null,
           ex
        );
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) {
        handlerExceptionResolver.resolveException(
            request,
            response,
            null,
           ex
        );
    }

    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       RateLimitExceededException ex) {
        handlerExceptionResolver.resolveException(
            request,
            response,
            null,
            ex);
    }
}
