package io.github.susimsek.springbootjweauthjpademo.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class UserPrincipal implements UserDetails {

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
    public String getUsername() {
        return id;
    }
}
