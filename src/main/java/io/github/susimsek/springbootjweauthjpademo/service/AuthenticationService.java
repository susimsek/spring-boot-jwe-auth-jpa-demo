package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.domain.Authority;
import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.domain.UserAuthorityMapping;
import io.github.susimsek.springbootjweauthjpademo.dto.request.RegisterRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.ResetPasswordRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.EmailVerificationDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ForgotPasswordDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.LoginResultDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ProfileDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.RefreshTokenDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.RefreshTokenResultDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.RegistrationDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ResetPasswordDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.TokenDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.VerifyTotpResultDTO;
import io.github.susimsek.springbootjweauthjpademo.exception.AccountLockedException;
import io.github.susimsek.springbootjweauthjpademo.exception.InvalidCredentialsException;
import io.github.susimsek.springbootjweauthjpademo.exception.InvalidTokenException;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceAlreadyExistsException;
import io.github.susimsek.springbootjweauthjpademo.mapper.AuthorityMapper;
import io.github.susimsek.springbootjweauthjpademo.mapper.MfaMapper;
import io.github.susimsek.springbootjweauthjpademo.mapper.UserMapper;
import io.github.susimsek.springbootjweauthjpademo.repository.AuthorityRepository;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import io.github.susimsek.springbootjweauthjpademo.security.AuthoritiesConstants;
import io.github.susimsek.springbootjweauthjpademo.security.JwtUtil;
import io.github.susimsek.springbootjweauthjpademo.security.RandomUtil;
import io.github.susimsek.springbootjweauthjpademo.security.TotpAuthenticationToken;
import io.github.susimsek.springbootjweauthjpademo.security.TotpUtil;
import io.github.susimsek.springbootjweauthjpademo.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthorityRepository authorityRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final TotpUtil totpUtil;
    private final MfaMapper mfaMapper;
    private final UserMapper userMapper;
    private final UserCacheService userCacheService;
    private final AuthorityMapper authorityMapper;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public RegistrationDTO register(RegisterRequestDTO req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ResourceAlreadyExistsException(
                ProblemType.USERNAME_CONFLICT,
                "username",
                req.username()
            );
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ResourceAlreadyExistsException(
                ProblemType.EMAIL_CONFLICT,
                "email",
                req.email()
            );
        }

        Authority authority = authorityRepository
            .findByName(AuthoritiesConstants.USER)
            .orElseThrow(() ->
                new IllegalStateException("Cannot register user. Required role 'ROLE_USER' is missing.")
            );

        User user = userMapper.toEntity(req);
        user.setPassword(passwordEncoder.encode(req.password()));

        if (req.mfaEnabled()) {
            String secret = totpUtil.generateSecret();
            user.setMfaSecret(secret);
        }

        String verificationToken = RandomUtil.generateRandomAlphanumeric();
        user.setEmailVerificationToken(verificationToken);
        user.setEmailVerificationExpiresAt(
            Instant.now().plus(24, ChronoUnit.HOURS));

        UserAuthorityMapping mapping = new UserAuthorityMapping();
        mapping.setUser(user);
        mapping.setAuthority(authority);
        user.getAuthorities().add(mapping);

        User saved = userRepository.save(user);
        userCacheService.clearUserCaches(saved);

        emailService.sendVerificationEmail(saved.getEmail(), verificationToken);

        return userMapper.toRegistrationDto(saved);
    }

    public LoginResultDTO login(String username, String password) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(username, password);
            Authentication auth = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(auth);

            UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
            // Build profile
            ProfileDTO profile = userMapper.toProfileDto(principal);
            if (principal.isMfaEnabled()) {
                // Issue an MFA‐only token
                var mfaToken = jwtUtil.generateMfaToken(principal.getId());
                return new LoginResultDTO(profile, null,null,
                    mfaToken, !principal.isMfaVerified());
            } else {
                // Issue a full access token
                List<String> roles = authorityMapper.toAuthorityNames(principal.getAuthorities());
                var accessToken = jwtUtil.generateAccessToken(principal.getId(), roles, false);
                var refreshToken = refreshTokenService.createRefreshToken(principal.getId());

                return new LoginResultDTO(
                    profile, accessToken, refreshToken,null, null);
            }
        } catch (AuthenticationException ex) {
            if (ex.getCause() instanceof AccountLockedException accountLockedException) {
                throw accountLockedException;
            }
            throw new InvalidCredentialsException(ProblemType.INVALID_CREDENTIALS);
        }
    }

    public VerifyTotpResultDTO verifyTotp(String userId, String code) {
        TotpAuthenticationToken totpAuth = new TotpAuthenticationToken(userId, code);
        Authentication auth = authenticationManager.authenticate(totpAuth);
        SecurityContextHolder.getContext().setAuthentication(auth);

        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

        List<String> roles = authorityMapper.toAuthorityNames(principal.getAuthorities());

        var accessToken = jwtUtil.generateAccessToken(principal.getId(), roles, true);
        var refreshToken = refreshTokenService.createRefreshToken(principal.getId());

        return mfaMapper.toVerifyTotpResultDTO(principal, accessToken, refreshToken);

    }

    public void logout(String userId) {
        refreshTokenService.deleteByUserId(userId);
    }

    public EmailVerificationDTO verifyEmail(String token) {

        User user = userRepository.findOneWithAuthoritiesByEmailVerificationToken(token)
            .orElseThrow(() ->
                new InvalidTokenException(ProblemType.INVALID_VERIFICATION_TOKEN)
            );

        if (user.isEmailVerified()) {
            throw new ResourceAlreadyExistsException(
                ProblemType.EMAIL_ALREADY_VERIFIED,
                "email",
                user.getEmail()
            );
        }

        if (user.getEmailVerificationExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException(ProblemType.VERIFICATION_LINK_EXPIRED);
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiresAt(null);

        EmailVerificationDTO emailVerificationDTO;
        if (user.isMfaEnabled()) {
            TokenDTO mfaDto = jwtUtil.generateMfaToken(user.getId());
            emailVerificationDTO = new EmailVerificationDTO(
                "VERIFIED", true, mfaDto);
        } else {
            user.setEnabled(true);
            emailVerificationDTO = new EmailVerificationDTO(
                "VERIFIED", false, null);
        }

        User saved = userRepository.save(user);
        userCacheService.clearUserCaches(saved);

        // If MFA not enabled, no MFA token
        return emailVerificationDTO;
    }

    public ForgotPasswordDTO forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = RandomUtil.generateRandomAlphanumeric();
            user.setPasswordResetToken(token);
            user.setPasswordResetExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

            User saved = userRepository.save(user);
            userCacheService.clearUserCaches(saved);

            emailService.sendPasswordResetEmail(user.getEmail(), token);
        });
        return new ForgotPasswordDTO("QUEUED");
    }

    public ResetPasswordDTO resetPassword(ResetPasswordRequestDTO dto) {
        User user = userRepository
            .findByPasswordResetToken(dto.token())
            .orElseThrow(() -> new InvalidTokenException(ProblemType.RESET_TOKEN_INVALID));

        if (user.getPasswordResetExpiresAt() == null
            || user.getPasswordResetExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException(ProblemType.RESET_LINK_EXPIRED);
        }

        user.setPassword(passwordEncoder.encode(dto.newPassword()));
        user.setPasswordResetToken(null);
        user.setPasswordResetExpiresAt(null);

        if (user.isLocked()) {
            user.setFailedAttempt(0);
            user.setLocked(false);
            user.setLockTime(null);
            user.setLockExpiresAt(null);
        }

        User saved = userRepository.save(user);
        userCacheService.clearUserCaches(saved);

        return new ResetPasswordDTO("SUCCESS");
    }

    public RefreshTokenResultDTO refreshToken(Jwt jwt) {
        RefreshTokenDTO refreshDto = refreshTokenService.rotateRefreshToken(jwt);
        String userId = refreshDto.subject();

        User user = userRepository.findOneWithAuthoritiesById(userId)
            .orElseThrow(() -> new JwtException("Invalid refresh token subject"));
        List<String> roles = authorityMapper.toAuthorityNames(user.getAuthorities());

        TokenDTO accessDto = jwtUtil.generateAccessToken(userId, roles, user.isMfaVerified());

        return new RefreshTokenResultDTO(accessDto, refreshDto.refreshToken());
    }
}
