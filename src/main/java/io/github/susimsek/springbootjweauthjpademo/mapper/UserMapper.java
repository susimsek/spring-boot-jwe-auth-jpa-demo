package io.github.susimsek.springbootjweauthjpademo.mapper;

import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.dto.request.CreateUserRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.PartialUpdateUserRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.RegisterRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateUserRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ProfileDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.RegistrationDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.UserDTO;
import io.github.susimsek.springbootjweauthjpademo.security.UserPrincipal;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;

@Mapper(componentModel = "spring", uses = AuthorityMapper.class)
public interface UserMapper {
    UserDTO toUserDto(User user);

    RegistrationDTO toRegistrationDto(User user);

    ProfileDTO toProfileDto(User user);

    @Mapping(target = "username", source = "login")
    ProfileDTO toProfileDto(UserPrincipal principal);

    @Mapping(target = "login", source = "username")
    @Mapping(target = "accountNonExpired",    constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    UserPrincipal toPrincipal(User entity);

    @Mapping(target = "login", source = "user.username")
    @Mapping(target = "accountNonExpired",    constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    @Mapping(target = "authorities", source = "user.authorities")
    UserPrincipal toPrincipal(User user, OAuth2User oauth2User);

    @Mapping(target = "login", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "accountNonExpired",    constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    @Mapping(target = "authorities", source = "user.authorities")
    UserPrincipal toPrincipal(User user, OidcUser oidcUser);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // set in service
    @Mapping(target = "locked", constant = "false")
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "mfaVerified", constant = "false")
    @Mapping(target = "authorities", ignore = true)
    User toEntity(CreateUserRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // set in service
    @Mapping(target = "emailVerified", constant = "false")
    @Mapping(target = "mfaVerified", constant = "false")
    @Mapping(target = "enabled", constant = "false")
    @Mapping(target = "locked", constant = "false")
    @Mapping(target = "protectedFlag", constant = "false")
    @Mapping(target = "authorities", ignore = true)
    User toEntity(RegisterRequestDTO dto);

    @Mapping(target = "authorities", ignore = true)
    void updateUser(UpdateUserRequestDTO dto, @MappingTarget User entity);

    @Mapping(target = "authorities", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void partialUpdate(PartialUpdateUserRequestDTO dto, @MappingTarget User entity);
}
