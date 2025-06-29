package io.github.susimsek.springbootjweauthjpademo.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.jcache.configuration.CaffeineConfiguration;
import com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.domain.Authority;
import io.github.susimsek.springbootjweauthjpademo.domain.Message;
import io.github.susimsek.springbootjweauthjpademo.domain.RefreshToken;
import io.github.susimsek.springbootjweauthjpademo.domain.User;
import io.github.susimsek.springbootjweauthjpademo.domain.UserAuthorityMapping;
import io.github.susimsek.springbootjweauthjpademo.repository.AuthorityRepository;
import io.github.susimsek.springbootjweauthjpademo.repository.UserRepository;
import io.github.susimsek.springbootjweauthjpademo.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.hibernate.cache.jcache.ConfigSettings;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import java.util.OptionalLong;

@Configuration(proxyBeanMethods = false)
@EnableCaching
@RequiredArgsConstructor
public class CacheConfig {

    private final ApplicationProperties applicationProperties;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(buildCaffeineConfig(
            applicationProperties.getCache().getCaffeine().getDefaultConfig()));
        return cacheManager;
    }

    private Caffeine<Object, Object> buildCaffeineConfig(
        ApplicationProperties.Cache.Caffeine.CacheConfig config) {
        return Caffeine.newBuilder()
            .expireAfterWrite(config.getTtl())
            .initialCapacity(config.getInitialCapacity())
            .maximumSize(config.getMaximumSize())
            .recordStats();
    }


    @ConditionalOnProperty(name = "spring.jpa.properties.hibernate.cache.use_second_level_cache",
        havingValue = "true")
    @RequiredArgsConstructor
    static class HibernateSecondLevelCacheConfiguration {

        private final ApplicationProperties applicationProperties;

        @Bean
        public javax.cache.CacheManager jcacheManager(JCacheManagerCustomizer jcacheManagerCustomizer) {
            var cachingProvider = Caching.getCachingProvider(CaffeineCachingProvider.class.getName());
            var manager = cachingProvider.getCacheManager();
            jcacheManagerCustomizer.customize(manager);
            return manager;
        }

        @Bean
        public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(javax.cache.CacheManager jcacheManager) {
            return props -> props.put(ConfigSettings.CACHE_MANAGER, jcacheManager);
        }

        @Bean
        public JCacheManagerCustomizer cacheManagerCustomizer() {
            return cm -> {
                createCache(cm, "default-update-timestamps-region");
                createCache(cm, "default-query-results-region");
                createCache(cm, UserRepository.USERS_BY_USERNAME_CACHE);
                createCache(cm, UserRepository.USERS_BY_ID_CACHE);
                createCache(cm, AuthorityRepository.AUTHORITIES_BY_NAME_CACHE);
                createCache(cm, User.class.getName());
                createCache(cm, Authority.class.getName());
                createCache(cm, User.class.getName() + ".authorities");
                createCache(cm, UserAuthorityMapping.class.getName());
                createCache(cm, RefreshToken.class.getName());
                createCache(cm, Message.class.getName());
                createCache(cm, MessageService.MESSAGES_BY_LOCALE_CACHE);
            };
        }

        private void createCache(javax.cache.CacheManager cm, String cacheName) {
            var existing = cm.getCache(cacheName);
            if (existing != null) {
                existing.clear();
                return;
            }
            var caffeineProperties = applicationProperties.getCache().getCaffeine();
            var config = caffeineProperties.getCaches().getOrDefault(cacheName,
                caffeineProperties.getDefaultConfig());
            var caffeineConfig = new CaffeineConfiguration<>();
            caffeineConfig.setMaximumSize(OptionalLong.of(config.getMaximumSize()));
            caffeineConfig.setExpireAfterWrite(OptionalLong.of(config.getTtl().toNanos()));
            caffeineConfig.setStatisticsEnabled(true);
            cm.createCache(cacheName, caffeineConfig);
        }
    }
}
