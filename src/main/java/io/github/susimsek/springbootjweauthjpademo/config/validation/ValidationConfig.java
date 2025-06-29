package io.github.susimsek.springbootjweauthjpademo.config.validation;

import io.github.susimsek.springbootjweauthjpademo.config.i18n.DatabaseMessageInterpolator;
import io.github.susimsek.springbootjweauthjpademo.service.MessageLookupService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration(proxyBeanMethods = false)
public class ValidationConfig {

    @Bean
    public LocalValidatorFactoryBean validator(MessageLookupService messageLookupService) {
        LocalValidatorFactoryBean vb = new LocalValidatorFactoryBean();
        vb.setMessageInterpolator(new DatabaseMessageInterpolator(messageLookupService));
        return vb;
    }
}
