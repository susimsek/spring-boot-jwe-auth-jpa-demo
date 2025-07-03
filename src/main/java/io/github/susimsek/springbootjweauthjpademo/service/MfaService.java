package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.dto.request.ConfirmTotpRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.MfaStatusDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.TokenDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.TotpDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.VerifyTotpDTO;
import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.exception.InvalidCredentialsException;
import io.github.susimsek.springbootjweauthjpademo.exception.InvalidOtpException;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceAlreadyExistsException;
import io.github.susimsek.springbootjweauthjpademo.mapper.AuthorityMapper;
import io.github.susimsek.springbootjweauthjpademo.mapper.MfaMapper;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import io.github.susimsek.springbootjweauthjpademo.security.JwtUtil;
import io.github.susimsek.springbootjweauthjpademo.security.TotpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TotpUtil totpUtil;
    private final JwtUtil jwtUtil;
    private final UserLookupService userLookupService;
    private final MfaMapper mfaMapper;
    private final AuthorityMapper authorityMapper;
    private final UserCacheService userCacheService;
    private final RefreshTokenService refreshTokenService;

    public MfaStatusDTO getMfaStatus(String userId) {
        User user = userLookupService.findByIdOrThrow(userId);
        return new MfaStatusDTO(user.isMfaEnabled(), user.isMfaVerified());
    }

    public TotpDTO setupMfa(String userId, String currentPassword) {
        User user = userLookupService.findByIdOrThrow(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException(ProblemType.INVALID_PASSWORD);
        }
        if (user.isMfaEnabled() && user.isMfaVerified()) {
            String secret = user.getMfaSecret();
            return new TotpDTO(secret, totpUtil.generateQrUrl(user.getUsername(), secret));
        }
        String secret = totpUtil.generateSecret();
        user.setMfaEnabled(true);
        user.setMfaVerified(false);
        user.setMfaSecret(secret);

        User savedUser = userRepository.save(user);
        userCacheService.clearUserCaches(savedUser);

        String qrUrl = totpUtil.generateQrUrl(user.getUsername(), secret);
        return new TotpDTO(secret, qrUrl);
    }

    public TotpDTO setupMfa(String userId) {
        User user = userLookupService.findByIdOrThrow(userId);

        if (!user.isMfaEnabled()) {
            throw new BadCredentialsException("MFA not enabled for user");
        }

        String secret = user.getMfaSecret();
        String qrCodeUrl = totpUtil.generateQrUrl(user.getUsername(), secret);

        return new TotpDTO(secret, qrCodeUrl);
    }

    public VerifyTotpDTO confirmMfa(String userId,
                                    ConfirmTotpRequestDTO verifyTotpRequest) {
        User user = userLookupService.findByIdWithAuthoritiesOrThrow(userId);
        if (!user.isMfaEnabled()) {
            throw new ResourceAlreadyExistsException(
                ProblemType.MFA_NOT_ENABLED,
                "mfa",
                userId
            );
        }
        if (user.isMfaVerified()) {
            throw new ResourceAlreadyExistsException(
                ProblemType.MFA_ALREADY_VERIFIED,
                "mfa",
                userId
            );
        }
        if (!totpUtil.verifyCode(user.getMfaSecret(), verifyTotpRequest.code())) {
            throw new InvalidOtpException("Invalid TOTP code");
        }
        user.setMfaVerified(true);
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        userCacheService.clearUserCaches(savedUser);

        boolean loginFlow = "login".equalsIgnoreCase(verifyTotpRequest.flow());

        TokenDTO accessToken = null;
        TokenDTO refreshToken = null;
        if (loginFlow) {
            List<String> roles = authorityMapper.toAuthorityNames(user.getAuthorities());
            accessToken = jwtUtil.generateAccessToken(user.getId(),  roles,true);
            refreshToken = refreshTokenService.createRefreshToken(user.getId());
        }

        return mfaMapper.toVerifyTotpDto(true, accessToken, refreshToken, user);
    }

    public void disableMfa(String userId, String currentPassword) {
        User user = userLookupService.findByIdOrThrow(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException(ProblemType.INVALID_PASSWORD);
        }
        if (!user.isMfaEnabled()) {
            throw new ResourceAlreadyExistsException(
                ProblemType.MFA_ALREADY_DISABLED,
                "mfa",
                userId
            );
        }
        user.setMfaEnabled(false);
        user.setMfaVerified(false);
        user.setMfaSecret(null);

        User savedUser = userRepository.save(user);
        userCacheService.clearUserCaches(savedUser);
    }

    public TotpDTO regenerateMfa(String userId, String currentPassword) {
        User user = userLookupService.findByIdOrThrow(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidCredentialsException(ProblemType.INVALID_PASSWORD);
        }
        if (!user.isMfaEnabled()) {
            throw new ResourceAlreadyExistsException(
                ProblemType.MFA_NOT_ENABLED,
                "mfa",
                userId
            );
        }
        String newSecret = totpUtil.generateSecret();
        user.setMfaSecret(newSecret);
        user.setMfaVerified(false);

        User savedUser = userRepository.save(user);
        userCacheService.clearUserCaches(savedUser);

        return new TotpDTO(newSecret, totpUtil.generateQrUrl(user.getUsername(), newSecret));
    }
}
