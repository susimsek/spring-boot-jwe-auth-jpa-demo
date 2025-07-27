package io.github.susimsek.springbootjweauthjpademo.security.oauth2;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

import java.util.Map;


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
