package io.github.susimsek.springbootjweauthjpademo.config.restclient;

import io.github.susimsek.springbootjweauthjpademo.config.logging.LoggingClientHttpRequestInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RestClientConfig {

    @Bean
    public RestClientCustomizer restClientCustomizer(
        ObjectProvider<LoggingClientHttpRequestInterceptor> loggingClientHttpRequestInterceptor) {
        return builder -> loggingClientHttpRequestInterceptor.ifAvailable(
            builder::requestInterceptor
        );
    }

}
