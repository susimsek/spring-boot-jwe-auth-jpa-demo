package io.github.susimsek.springbootjweauthjpademo.config.ratelimiter;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.github.susimsek.springbootjweauthjpademo.aspect.RateLimiterAspect;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.config.spel.SpelResolver;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemSupport;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import jakarta.servlet.DispatcherType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.Ordered;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;

import java.time.Duration;

@Configuration(proxyBeanMethods = false)
@EnableAspectJAutoProxy
public class RateLimitConfig {

    private static final String FILTER_NAME = "rateLimiterFilter";

    @Bean
    public ProxyManager<String> proxyManager(RedisProperties redisProperties,
                                             LettuceConnectionFactory redisConnectionFactory) {
        RedisProperties.Cluster cluster = redisProperties.getCluster();
        if (cluster != null) {
            RedisClusterClient client = (RedisClusterClient)  redisConnectionFactory.getNativeClient();
            StatefulRedisClusterConnection<String, byte[]> conn = client.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
            );
            return Bucket4jLettuce
                .casBasedBuilder(conn)
                .expirationAfterWrite(
                    ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                        Duration.ofHours(24)
                    )
                )
                .build();
        } else {
            // Standalone
            RedisClient client = (RedisClient)  redisConnectionFactory.getNativeClient();
            StatefulRedisConnection<String, byte[]> conn = client.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE)
            );
            return Bucket4jLettuce
                .casBasedBuilder(conn)
                .expirationAfterWrite(
                    ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                        Duration.ofHours(24)
                    )
                )
                .build();
        }
    }

    @Bean
    public RateLimiterAspect rateLimiterAspect(ProxyManager<String> proxyManager,
                                             ApplicationProperties props,
                                             SpelResolver spelResolver) {
        return new RateLimiterAspect(proxyManager, props, spelResolver);
    }

    @ConditionalOnProperty(prefix = "application.rateLimiter", name = "filterEnabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public FilterRegistrationBean<RateLimiterFilter> rateLimiterFilter(
        ProxyManager<String> proxyManager,
        ApplicationProperties props,
        ProblemSupport problemSupport
    ) {
        RateLimiterFilter filter = new RateLimiterFilter(proxyManager, props.getRateLimiter(), problemSupport);
        FilterRegistrationBean<RateLimiterFilter> registration = new FilterRegistrationBean<>();
        registration.setName(FILTER_NAME);
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        return registration;
    }
}
