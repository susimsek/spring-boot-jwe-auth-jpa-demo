package io.github.susimsek.springbootjweauthjpademo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MessageCacheService {

    private final CacheManager cacheManager;

    public void clearMessageCache(String locale) {
        Objects.requireNonNull(
            cacheManager.getCache(MessageService.MESSAGES_BY_LOCALE_CACHE),
            "messagesByLocale cache not configured"
        ).evict(locale);
    }
}
