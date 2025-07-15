package io.github.susimsek.springbootjweauthjpademo.config.security;

import io.github.susimsek.springbootjweauthjpademo.client.RecaptchaClient;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.exception.ProblemSupport;
import io.github.susimsek.springbootjweauthjpademo.security.DefaultRecaptchaTokenResolver;
import io.github.susimsek.springbootjweauthjpademo.security.RecaptchaFilter;
import io.github.susimsek.springbootjweauthjpademo.security.RecaptchaService;
import io.github.susimsek.springbootjweauthjpademo.security.RecaptchaTokenResolver;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class SecurityRecaptchaConfig {

    private final ApplicationProperties props;

    private static final String FILTER_NAME = "recaptchaFilter";

    @Bean
    public RecaptchaClient recaptchaClient(
        RestClient.Builder restClientBuilder
    ) {
        RestClient restClient = restClientBuilder
            .baseUrl(props.getSecurity().getRecaptcha().getUrl())
            .build();
        var adapter = RestClientAdapter.create(restClient);
        var factory = HttpServiceProxyFactory.builderFor(adapter).build();
        return factory.createClient(RecaptchaClient.class);
    }

    @Bean
    public RecaptchaTokenResolver recaptchaTokenResolver() {
        return new DefaultRecaptchaTokenResolver();
    }

    @Bean
    public FilterRegistrationBean<RecaptchaFilter> recaptchaFilter(RecaptchaService recaptchaService,
                                                                   ProblemSupport problemSupport,
                                                                   RecaptchaTokenResolver recaptchaTokenResolver) {
        RecaptchaFilter filter = new RecaptchaFilter(recaptchaService, problemSupport, recaptchaTokenResolver);
        FilterRegistrationBean<RecaptchaFilter> registration = new FilterRegistrationBean<>();
        registration.setName(FILTER_NAME);
        registration.setFilter(filter);
        registration.addUrlPatterns( "/api/*");
        registration.setDispatcherTypes(DispatcherType.REQUEST);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
    }
}
