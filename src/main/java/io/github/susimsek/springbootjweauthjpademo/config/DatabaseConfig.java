package io.github.susimsek.springbootjweauthjpademo.config;

import io.github.susimsek.springbootjweauthjpademo.config.h2.H2ConfigurationHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.sql.SQLException;

@Configuration(proxyBeanMethods = false)
@EnableJpaRepositories("io.github.susimsek.springbootjweauthjpademo.repository")
@EnableJpaAuditing(auditorAwareRef = "springSecurityAuditorAware")
@EnableTransactionManagement
@RequiredArgsConstructor
@Slf4j
public class DatabaseConfig {

    private final ApplicationProperties applicationProperties;

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "spring.h2.console", name = "enabled", havingValue = "true")
    public Object h2TCPServer() throws SQLException {
        return H2ConfigurationHelper.createServer(
            applicationProperties.getDatabase().getH2().getEmbedded().getPort());
    }
}
