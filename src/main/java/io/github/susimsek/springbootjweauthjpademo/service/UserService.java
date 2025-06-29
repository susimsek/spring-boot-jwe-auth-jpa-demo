package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.domain.Authority;
import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.domain.UserAuthorityMapping;
import io.github.susimsek.springbootjweauthjpademo.dto.filter.UserFilter;
import io.github.susimsek.springbootjweauthjpademo.dto.request.CreateUserRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.PartialUpdateUserRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateUserRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.UserDTO;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceConflictException;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceNotFoundException;
import io.github.susimsek.springbootjweauthjpademo.exception.ValidationException;
import io.github.susimsek.springbootjweauthjpademo.mapper.UserMapper;
import io.github.susimsek.springbootjweauthjpademo.repository.AuthorityRepository;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import io.github.susimsek.springbootjweauthjpademo.security.TotpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final UserCacheService userCacheService;
    private final TotpUtil totpUtil;
    private final ApplicationProperties applicationProperties;

    public Page<UserDTO> listUsers(Pageable pageable,
                                   UserFilter filter) {
        return userRepository.findAll(filter.toSpecification(), pageable)
            .map(userMapper::toUserDto);
    }

    public UserDTO getUser(String id) {
        User user = userRepository.findOneWithAuthoritiesById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.USER_NOT_FOUND,
                "id",
                id
            ));
        return userMapper.toUserDto(user);
    }

    @Transactional
    public UserDTO createUser(CreateUserRequestDTO req) {
        // Check conflicts
        if (userRepository.existsByUsername(req.username())) {
            throw new ResourceConflictException(
                ProblemType.USERNAME_CONFLICT,
                "username",
                req.username()
            );
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ResourceConflictException(
                ProblemType.EMAIL_CONFLICT,
                "email",
                req.email()
            );
        }
        // Build domain
        User u = userMapper.toEntity(req);
        u.setPassword(passwordEncoder.encode(req.password()));
        createAuthorities(u, req.authorities());

        User saved = userRepository.save(u);
        userCacheService.clearUserCaches(saved);
        return userMapper.toUserDto(saved);
    }


    @Transactional
    public UserDTO updateUser(String id, UpdateUserRequestDTO req) {
        User u = userRepository.findOneWithAuthoritiesById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.USER_NOT_FOUND,
                "id",
                id
            ));
        if (!u.getUsername().equals(req.username()) && userRepository.existsByUsername(req.username())) {
            throw new ResourceConflictException(
                ProblemType.USERNAME_CONFLICT,
                "username",
                req.username()
            );
        }

        if (!u.getEmail().equals(req.email()) && userRepository.existsByEmail(req.email())) {
            throw new ResourceConflictException(
                ProblemType.EMAIL_CONFLICT,
                "email",
                req.email()
            );
        }
        boolean wasMfaEnabled = u.isMfaEnabled();
        // Full update via MapStruct (all fields are required in UpdateUserRequestDTO)
        userMapper.updateUser(req, u);
        if (req.mfaEnabled() && !wasMfaEnabled) {
            String newSecret = totpUtil.generateSecret();
            u.setMfaSecret(newSecret);
            u.setMfaVerified(false);
        }
        // If MFA was disabled, clear related fields
        if (!req.mfaEnabled()) {
            u.setMfaVerified(false);
            u.setMfaSecret(null);
        }

        applyLockState(u, req.locked());

        updateAuthorities(u, req.authorities());

        User updated = userRepository.save(u);
        userCacheService.clearUserCaches(updated);
        return userMapper.toUserDto(updated);
    }

    @Transactional
    public UserDTO patchUser(String id, PartialUpdateUserRequestDTO req) {
        User u = userRepository.findOneWithAuthoritiesById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.USER_NOT_FOUND,
                "id",
                id
            ));

        // Conflict checks for username and email
        if (req.username() != null && !u.getUsername().equals(req.username())
            && userRepository.existsByUsername(req.username())) {
            throw new ResourceConflictException(
                ProblemType.USERNAME_CONFLICT,
                "username",
                req.username()
            );
        }
        if (req.email() != null && !u.getEmail().equals(req.email())
            && userRepository.existsByEmail(req.email())) {
            throw new ResourceConflictException(
                ProblemType.EMAIL_CONFLICT,
                "email",
                req.email()
            );
        }

        // Track previous MFA enabled state
        boolean wasMfaEnabled = u.isMfaEnabled();

        // Apply partial update via MapStruct
        userMapper.partialUpdate(req, u);
        // If MFA was disabled, clear related fields
        if (req.mfaEnabled() != null && !req.mfaEnabled()) {
            u.setMfaVerified(false);
            u.setMfaSecret(null);
        }

        // If MFA was just enabled, generate secret and set verified to false
        if (req.mfaEnabled() != null && req.mfaEnabled() && !wasMfaEnabled) {
            String newSecret = totpUtil.generateSecret();
            u.setMfaSecret(newSecret);
            u.setMfaVerified(false);
        }

        if (req.authorities() != null) {
            updateAuthorities(u, req.authorities());
        }

        if (req.locked() != null) {
            applyLockState(u, req.locked());
        }

        User updated = userRepository.save(u);
        userCacheService.clearUserCaches(updated);
        return userMapper.toUserDto(updated);
    }

    @Transactional
    public void deleteUser(String id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                ProblemType.USER_NOT_FOUND,
                "id",
                id
            ));
        userRepository.delete(user);
        userCacheService.clearUserCaches(user);
    }

    private void createAuthorities(User user, Set<String> names) {
        for (String name : names) {
            Authority auth = authorityRepository.findByName(name)
                .orElseThrow(() -> new ValidationException(
                    ProblemType.INVALID_AUTHORITY,
                    name
                ));
            UserAuthorityMapping m = new UserAuthorityMapping();
            m.setUser(user);
            m.setAuthority(auth);
            user.getAuthorities().add(m);
        }
    }
    private void updateAuthorities(User u, Set<String> authorityNames) {
        // Sync authorities: remove those not in request
        Set<String> newAuths = new HashSet<>(authorityNames);
        u.getAuthorities().removeIf(m -> !newAuths.contains(m.getAuthority().getName()));
        // Add any new authorities
        for (String name : newAuths) {
            boolean exists = u.getAuthorities().stream()
                .anyMatch(m -> m.getAuthority().getName().equals(name));
            if (!exists) {
                Authority auth = authorityRepository.findByName(name)
                    .orElseThrow(() -> new ValidationException(
                        ProblemType.INVALID_AUTHORITY,
                        name
                    ));
                UserAuthorityMapping m = new UserAuthorityMapping();
                m.setUser(u);
                m.setAuthority(auth);
                u.getAuthorities().add(m);
            }
        }
    }

    private void applyLockState(User u, boolean locked) {
        if (locked) {
            Instant now = Instant.now();
            u.setLockTime(now);
            u.setLockExpiresAt(now.plus(applicationProperties.getSecurity().getLock().getLockDuration()));
        } else {
            u.setFailedAttempt(0);
            u.setLockTime(null);
            u.setLockExpiresAt(null);
        }
    }
}
