package io.github.susimsek.springbootjweauthjpademo.config.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.susimsek.springbootjweauthjpademo.aspect.LoggingAspect;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class LoggingConfig {

    private final ApplicationProperties applicationProperties;

    private static final String FILTER_NAME = "loggingFilter";

    @Bean
    public LogFormatter jsonLogFormatter(ObjectMapper objectMapper) {
        return new JsonLogFormatter(objectMapper);
    }

    @Bean
    public Obfuscator obfuscator(ObjectMapper objectMapper) {
        return new Obfuscator(objectMapper, applicationProperties);
    }


    @ConditionalOnProperty(prefix = "application.logging", name = "filter-enabled", havingValue = "true", matchIfMissing = true)
    @Bean
    public FilterRegistrationBean<LoggingFilter> loggingFilter(LogFormatter logFormatter,
                                                               Obfuscator obfuscator) {
        LoggingFilter filter = new LoggingFilter(logFormatter, obfuscator, applicationProperties.getLogging());

        FilterRegistrationBean<LoggingFilter> registration = new FilterRegistrationBean<>();
        registration.setName(FILTER_NAME);
        registration.setFilter(filter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ASYNC);
        return registration;
    }

    @ConditionalOnProperty(prefix = "application.logging", name = "client-interceptor-enabled",
        havingValue = "true", matchIfMissing = true)
    @Bean
    public LoggingClientHttpRequestInterceptor loggingClientHttpRequestInterceptor(LogFormatter logFormatter,
                                                                                   Obfuscator obfuscator) {
        return new LoggingClientHttpRequestInterceptor(logFormatter, obfuscator, applicationProperties.getLogging());
    }


    @ConditionalOnProperty(prefix = "application.logging", name = "aspect-enabled", havingValue = "true")
    @Bean
    public LoggingAspect loggingAspect() {
        return new LoggingAspect();
    }
}
