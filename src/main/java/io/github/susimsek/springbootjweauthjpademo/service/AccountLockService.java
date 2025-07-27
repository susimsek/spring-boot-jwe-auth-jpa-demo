package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountLockService {

    private final UserRepository userRepository;
    private final UserCacheService userCacheService;
    private final ApplicationProperties applicationProperties;


    @Transactional
    public void recordFailedAttempts(String username) {
        userRepository.findByUsername(username)
            .ifPresent(this::recordFailedAttempts);
    }

    @Transactional
    public void resetFailedAttempts(String userId) {
        userRepository.resetAttemptsById(userId);
    }

    private boolean isLockExpired(User u) {
        return u.getLockExpiresAt() != null
            && Instant.now().isAfter(u.getLockExpiresAt());
    }

    private void saveAndEvict(User u) {
        User saved = userRepository.save(u);
        userCacheService.clearUserCaches(saved);
    }

    private void recordFailedAttempts(User u) {
        if (u.isEnabled() && !u.isLocked()) {
            int attempts = Optional.ofNullable(u.getFailedAttempt()).orElse(0) + 1;
            u.setFailedAttempt(attempts);

            if (attempts >= applicationProperties.getSecurity().getLock().getMaxFailedAttempts()) {
                u.lock(applicationProperties.getSecurity().getLock().getLockDuration());
            }
        } else if (u.isLocked() && isLockExpired(u)) {
            u.unlock();
        }
        saveAndEvict(u);
    }
}
