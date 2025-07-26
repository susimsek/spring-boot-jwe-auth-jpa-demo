package io.github.susimsek.springbootjweauthjpademo.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails, OidcUser {

    private final String id;
    private final String login;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String imageUrl;
    private final String password;

    private final boolean enabled;
    private final boolean locked;
    private final Instant lockTime;
    private final Instant lockExpiresAt;
    private final Integer failedAttempt;
    private final boolean protectedFlag;
    private final boolean accountNonExpired;
    private final boolean credentialsNonExpired;

    private final boolean mfaEnabled;
    private final boolean mfaVerified;
    private final String mfaSecret;
    private final Collection<? extends GrantedAuthority> authorities;
    private final transient Map<String, Object> attributes;


    private final transient OidcIdToken idToken;
    private final transient OidcUserInfo userInfo;
    private final transient Map<String, Object> claims;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonLocked() {
        return lockExpiresAt == null || Instant.now().isAfter(lockExpiresAt);
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return getUsername();
    }

    @Override
    public String getUsername() {
        return id;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return userInfo;
    }

    @Override
    public Map<String, Object> getClaims() {
        return claims;
    }
}
