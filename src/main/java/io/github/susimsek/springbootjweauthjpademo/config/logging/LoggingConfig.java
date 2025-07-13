package io.github.susimsek.springbootjweauthjpademo.config.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.loki4j.logback.JavaHttpSender;
import com.github.loki4j.logback.Loki4jAppender;
import com.github.loki4j.logback.PipelineConfigAppenderBase;
import io.github.susimsek.springbootjweauthjpademo.aspect.LoggingAspect;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

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

    @ConditionalOnProperty(name = "application.logging.loki.enabled", havingValue = "true")
    static class LokiLoggingConfig {

        private final ApplicationProperties.Logging.Loki props;
        private static final String LOKI_APPENDER_NAME = "LOKI";

        public LokiLoggingConfig(ApplicationProperties applicationProperties) {
            this.props = applicationProperties.getLogging().getLoki();
        }

        @Bean
        public Loki4jAppender loki4jAppender(Environment environment) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            var loki4jAppender = getLoki4jAppender(context, environment);

            Logger rootLogger = context.getLogger(ROOT_LOGGER_NAME);
            rootLogger.addAppender(loki4jAppender);
            return loki4jAppender;
        }

        private Loki4jAppender getLoki4jAppender(LoggerContext context,
                                                 Environment environment) {
            var loki4jAppender = new Loki4jAppender();
            loki4jAppender.setContext(context);
            loki4jAppender.setName(LOKI_APPENDER_NAME);

            var http = new PipelineConfigAppenderBase.HttpCfg();
            var httpSender = new JavaHttpSender();
            http.setSender(httpSender);
            http.setUrl(props.getHttp().getUrl());
            http.setRequestTimeoutMs(props.getHttp().getRequestTimeout().toMillis());
            loki4jAppender.setHttp(http);

            var batch = new PipelineConfigAppenderBase.BatchCfg();
            batch.setMaxItems(props.getBatch().getMaxItems());
            batch.setTimeoutMs(props.getBatch().getTimeout().toMillis());
            loki4jAppender.setBatch(batch);

            String applicationName = environment.getProperty("spring.application.name", "my-app");
            String pattern = environment.getProperty("logging.pattern.console",
                "%clr(%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX}){faint} %clr(%5p){highlight} %clr(${PID:- }" +
                    "){magenta} --- [%clr(${spring.application.name:-}){green},%X{traceId:-},%X{spanId:-}] [%clr(%t){faint}] " +
                    "%clr(%-40.40logger{39}){cyan} %clr(:){faint} %msg%n%clr(%wEx){red}");
            String hostname = environment.getProperty("HOSTNAME", "localhost");
            String labelPattern = (
                """
                    app=%s
                    host=%s
                    level=%%level"""
            ).formatted(
                applicationName, hostname
            );
            loki4jAppender.setReadMarkers(true);
            loki4jAppender.setLabels(labelPattern);
            PatternLayout l = new PatternLayout();
            l.setPattern(pattern);
            loki4jAppender.setMessage(l);
            loki4jAppender.start();
            return loki4jAppender;
        }
    }
}
