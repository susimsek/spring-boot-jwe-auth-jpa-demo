package io.github.susimsek.springbootjweauthjpademo.security.oauth2;

public interface OAuth2UserInfo {
    String getProvider();
    String getId();
    String getEmail();
    String getName();
    String getFirstName();
    String getLastName();
    String getImageUrl();
}
