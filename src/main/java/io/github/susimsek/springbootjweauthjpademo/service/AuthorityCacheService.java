package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.domain.Authority;
import io.github.susimsek.springbootjweauthjpademo.repository.AuthorityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthorityCacheService {

    private final CacheManager cacheManager;

    public void clearAuthorityCaches(Authority authority) {
        Objects.requireNonNull(
            cacheManager.getCache(AuthorityRepository.AUTHORITIES_BY_NAME_CACHE),
            "authoritiesByName cache not configured"
        ).evictIfPresent(authority.getName());
    }
}
