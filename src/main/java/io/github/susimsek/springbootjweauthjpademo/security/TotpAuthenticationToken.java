package io.github.susimsek.springbootjweauthjpademo.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class TotpAuthenticationToken extends AbstractAuthenticationToken {

    private final Object principal;    // username
    private final Object credentials;  // TOTP code


    public TotpAuthenticationToken(String username, String code) {
        super(null);
        this.principal = username;
        this.credentials = code;
        setAuthenticated(false);
    }


    public TotpAuthenticationToken(Object principal,
                                   Object credentials,
                                   Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
