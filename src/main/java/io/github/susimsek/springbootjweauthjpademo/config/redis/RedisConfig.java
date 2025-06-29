package io.github.susimsek.springbootjweauthjpademo.config.redis;

import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnThreading;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.thread.Threading;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@AutoConfigureBefore(RedisAutoConfiguration.class)
public class RedisConfig {

    private final ApplicationProperties applicationProperties;

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "application.redis.embedded", name = "enabled", havingValue = "true")
    public Object embeddedRedisServer() {
        return EmbeddedRedisConfigurationHelper.createServer(
            applicationProperties.getRedis().getEmbedded().getPort());
    }

    @Bean
    @ConditionalOnBean(name = "embeddedRedisServer")
    @ConditionalOnThreading(Threading.PLATFORM)
    public LettuceConnectionFactory redisConnectionFactory(
        @Qualifier("embeddedRedisServer") Object embeddedRedisServer
    ) {
        return createConnectionFactory(embeddedRedisServer);
    }

    @Bean
    @ConditionalOnBean(name = "embeddedRedisServer")
    @ConditionalOnThreading(Threading.VIRTUAL)
    public LettuceConnectionFactory redisConnectionFactoryVirtualThreads(
        @Qualifier("embeddedRedisServer") Object embeddedRedisServer
    ) {
        LettuceConnectionFactory factory = createConnectionFactory(embeddedRedisServer);
        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor("redis-");
        executor.setVirtualThreads(true);
        factory.setExecutor(executor);
        return factory;
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);

        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    private LettuceConnectionFactory createConnectionFactory(Object embeddedRedisServer) {
        return new LettuceConnectionFactory(
            "localhost",
            EmbeddedRedisConfigurationHelper.extractPort(embeddedRedisServer)
        );
    }

}
