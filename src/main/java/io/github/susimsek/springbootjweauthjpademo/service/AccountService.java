package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.dto.request.ChangeEmailRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.ChangePasswordRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.DeleteAccountRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.request.UpdateProfileRequestDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ChangeEmailDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ChangePasswordDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.ProfileDTO;
import io.github.susimsek.springbootjweauthjpademo.exception.InvalidCredentialsException;
import io.github.susimsek.springbootjweauthjpademo.exception.InvalidTokenException;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemType;
import io.github.susimsek.springbootjweauthjpademo.exception.ResourceConflictException;
import io.github.susimsek.springbootjweauthjpademo.exception.ValidationException;
import io.github.susimsek.springbootjweauthjpademo.mapper.UserMapper;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import io.github.susimsek.springbootjweauthjpademo.security.RandomUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final UserLookupService userLookupService;
    private final UserMapper userMapper;
    private final UserCacheService userCacheService;
    private final RefreshTokenService refreshTokenService;


    public ChangePasswordDTO changePassword(String userId, ChangePasswordRequestDTO dto) {
        User user = userLookupService.findByIdOrThrow(userId);

        // 1) Verify current password
        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(ProblemType.INVALID_PASSWORD);
        }
        // 2) Ensure new password differs from current
        if (passwordEncoder.matches(dto.newPassword(), user.getPassword())) {
            throw new ValidationException(
                ProblemType.PASSWORD_CANNOT_REUSE,
                dto.newPassword()
            );
        }

        // 4) Update password
        user.setPassword(passwordEncoder.encode(dto.newPassword()));

        User saved = userRepository.save(user);
        userCacheService.clearUserCaches(saved);

        refreshTokenService.deleteByUserId(userId);

        return new ChangePasswordDTO("SUCCESS");
    }

    public ChangeEmailDTO changeEmail(String userId, ChangeEmailRequestDTO dto) {
        User user = userLookupService.findByIdOrThrow(userId);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(ProblemType.INVALID_PASSWORD);
        }

        String newEmail = dto.newEmail().trim().toLowerCase();

        // 3) Prevent changing to the same email
        if (newEmail.equalsIgnoreCase(user.getEmail())) {
            throw new ValidationException(
                ProblemType.EMAIL_CANNOT_REUSE,
                newEmail
            );
        }

        // 4) Check uniqueness of new email
        if (userRepository.existsByEmail(newEmail)) {
            throw new ResourceConflictException(
                ProblemType.EMAIL_CONFLICT,
                "email",
                newEmail
            );
        }

        // 5) Generate a new verification token and expiry date
        String verificationToken = RandomUtil.generateRandomAlphanumeric();
        Instant expiry = Instant.now().plus(24, ChronoUnit.HOURS);

        // 6) Update user fields
        user.setPendingEmail(newEmail);
        user.setEmailChangeToken(verificationToken);
        user.setEmailChangeTokenExpiresAt(expiry);

        User saved = userRepository.save(user);
        userCacheService.clearUserCaches(saved);

        // 7) Send verification email to the new address
        emailService.sendConfirmEmail(newEmail, verificationToken);

        // 8) Return response indicating success and that verification is pending
        return new ChangeEmailDTO("VERIFICATION_SENT", newEmail);
    }

    public ChangeEmailDTO confirmEmailChange(String token) {
        User user = userRepository.findByEmailChangeToken(token)
            .orElseThrow(() -> new InvalidTokenException(ProblemType.INVALID_VERIFICATION_TOKEN));

        if (user.getPendingEmail() == null) {
            throw new ResourceConflictException(
                ProblemType.NO_PENDING_EMAIL_CHANGE,
                "email"
            );
        }

        if (user.getEmailChangeTokenExpiresAt() == null
            || user.getEmailChangeTokenExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException(ProblemType.VERIFICATION_LINK_EXPIRED);
        }

        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setEmailChangeToken(null);
        user.setEmailChangeTokenExpiresAt(null);

        user.setEmailVerified(true);

        User saved = userRepository.save(user);
        userCacheService.clearUserCaches(saved);

        return new ChangeEmailDTO("SUCCESS", user.getEmail());
    }

    public ProfileDTO getProfile(String userId) {
        User user = userLookupService.findByIdWithAuthoritiesOrThrow(userId);
        return userMapper.toProfileDto(user);
    }

    @Transactional
    public ProfileDTO updateProfile(String userId, UpdateProfileRequestDTO dto) {
        User user = userLookupService.findByIdWithAuthoritiesOrThrow(userId);
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());

        User saved = userRepository.save(user);
        userCacheService.clearUserCaches(saved);

        return userMapper.toProfileDto(saved);
    }

    @Transactional
    public void deleteAccount(String userId, DeleteAccountRequestDTO dto) {
        User user = userLookupService.findByIdOrThrow(userId);

        if (!passwordEncoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException(ProblemType.INVALID_PASSWORD);
        }

        if (user.isProtectedFlag()) {
            throw new ResourceConflictException(
                ProblemType.USER_PROTECTED,
                "protected",
                userId
            );
        }

        refreshTokenService.deleteByUserId(userId);
        userRepository.delete(user);
        userCacheService.clearUserCaches(user);
    }
}
