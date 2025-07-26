package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.mapper.UserMapper;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);
        String email      = oauth2User.getAttribute("email");
        String givenName  = oauth2User.getAttribute("given_name");
        String familyName = oauth2User.getAttribute("family_name");
        String picture    = oauth2User.getAttribute("picture");
        String provider   = userRequest.getClientRegistration().getRegistrationId();

        User user = userRepository.findOneWithAuthoritiesByEmail(email)
            .orElseGet(() -> {
                User u = new User();
                u.setEmail(email);
                u.setUsername(email);
                u.setFirstName(givenName);
                u.setLastName(familyName);
                u.setImageUrl(picture);
                u.setEnabled(true);
                u.setMfaEnabled(false);
                u.setLocked(false);
                u.setMfaVerified(false);
                u.setEmailVerified(false);
                u.setProvider(provider);
                return userRepository.save(u);
            });

        return userMapper.toPrincipal(user, oauth2User);
    }
}
