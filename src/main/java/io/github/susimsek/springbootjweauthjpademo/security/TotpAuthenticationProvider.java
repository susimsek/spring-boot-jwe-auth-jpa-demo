package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.exception.InvalidOtpException;
import io.github.susimsek.springbootjweauthjpademo.exception.MfaSetupRequiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

@RequiredArgsConstructor
public class TotpAuthenticationProvider implements AuthenticationProvider {

    private final UserPrincipalService userDetailsService;
    private final TotpUtil totpUtil;

    @Override
    public Authentication authenticate(Authentication authentication) {
        String username = (String) authentication.getPrincipal();
        String code     = (String) authentication.getCredentials();

        UserDetails ud = userDetailsService.loadUserByUserId(username);

        if (!(ud instanceof UserPrincipal principal)) {
            throw new BadCredentialsException("Invalid user details type");
        }

        if (!principal.isMfaEnabled()) {
            throw new BadCredentialsException("MFA not enabled for user");
        }

        if (!principal.isMfaVerified()) {
            throw new MfaSetupRequiredException();
        }

        if (!totpUtil.verifyCode(principal.getMfaSecret(), code)) {
            throw new InvalidOtpException("Invalid TOTP code");
        }

        return new TotpAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TotpAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
