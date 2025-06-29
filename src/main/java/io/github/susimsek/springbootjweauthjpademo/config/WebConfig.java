package io.github.susimsek.springbootjweauthjpademo.config;

import io.github.susimsek.springbootjweauthjpademo.config.h2.H2ConfigurationHelper;
import io.github.susimsek.springbootjweauthjpademo.config.resolver.QrCodeSizeArgumentResolver;
import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
@Slf4j
public class WebConfig implements ServletContextInitializer, WebMvcConfigurer {

    private final Environment env;

    @Override
    public void onStartup(ServletContext servletContext) {
        if (env.getActiveProfiles().length != 0) {
            log.info("Web application configuration, using profiles: {}",
                (Object[]) env.getActiveProfiles());
        }

        if (h2ConsoleIsEnabled(env)) {
            initH2Console(servletContext);
        }
        log.info("Web application fully configured");
    }

    private boolean h2ConsoleIsEnabled(Environment env) {
        return (
            "true".equals(env.getProperty("spring.h2.console.enabled"))
        );
    }

    private void initH2Console(ServletContext servletContext) {
        log.debug("Initialize H2 console");
        H2ConfigurationHelper.initH2Console(servletContext);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new QrCodeSizeArgumentResolver());
    }
}
