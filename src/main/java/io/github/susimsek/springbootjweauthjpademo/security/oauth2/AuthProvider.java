package io.github.susimsek.springbootjweauthjpademo.security.oauth2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthProvider {
    GOOGLE("google");

    private final String providerId;
}
