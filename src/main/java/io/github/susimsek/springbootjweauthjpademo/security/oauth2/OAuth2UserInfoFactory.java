package io.github.susimsek.springbootjweauthjpademo.security.oauth2;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OAuth2UserInfoFactory {


    public static OAuth2UserInfo getOAuth2UserInfo(
        String registrationId,
        Map<String, Object> attributes) {
        if (AuthProvider.GOOGLE.getProviderId().equals(registrationId)) {
            return new GoogleOAuth2UserInfo(attributes);
        }
        throw new OAuth2AuthenticationException(
            "Login with " + registrationId + " is not supported"
        );
    }
}
