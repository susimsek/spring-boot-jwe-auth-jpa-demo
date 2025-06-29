package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserCacheService {

    private final CacheManager cacheManager;

    public void clearUserCaches(User user) {
        Objects.requireNonNull(
            cacheManager.getCache(UserRepository.USERS_BY_USERNAME_CACHE),
            "usersByUsername cache not configured"
        ).evictIfPresent(user.getUsername());

        Objects.requireNonNull(
            cacheManager.getCache(UserRepository.USERS_BY_ID_CACHE),
            "usersById cache not configured"
        ).evictIfPresent(user.getId());
    }
}
