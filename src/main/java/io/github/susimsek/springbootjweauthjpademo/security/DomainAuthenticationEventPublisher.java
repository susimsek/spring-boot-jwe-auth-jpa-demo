package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.service.AccountLockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

@Slf4j
@RequiredArgsConstructor
public class DomainAuthenticationEventPublisher implements AuthenticationEventPublisher {

    private final AccountLockService accountLockService;

    @Override
    public void publishAuthenticationSuccess(Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            return;
        }
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Integer attempt = userPrincipal.getFailedAttempt();
        if (attempt != null && attempt > 0) {
            accountLockService.resetFailedAttempts(authentication.getName());
        }
    }

    @Override
    public void publishAuthenticationFailure(
        AuthenticationException ex, Authentication authentication) {
        if (!(authentication instanceof UsernamePasswordAuthenticationToken)) {
            return;
        }
        accountLockService.recordFailedAttempts(authentication.getName());
    }
}
