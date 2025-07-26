package io.github.susimsek.springbootjweauthjpademo.security;

import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.mapper.UserMapper;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DomainOidcUserService extends OidcUserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        // Delegate to the default implementation for loading user info
        OidcUser oidcUser = super.loadUser(userRequest);

        String email      = oidcUser.getAttribute("email");
        String givenName  = oidcUser.getAttribute("given_name");
        String familyName = oidcUser.getAttribute("family_name");
        String picture    = oidcUser.getAttribute("picture");
        String provider   = userRequest.getClientRegistration().getRegistrationId();

        // Upsert the User entity
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

        return userMapper.toPrincipal(user, oidcUser);
    }
}
