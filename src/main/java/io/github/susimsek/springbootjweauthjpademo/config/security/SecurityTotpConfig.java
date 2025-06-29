package io.github.susimsek.springbootjweauthjpademo.config.security;

import com.google.zxing.qrcode.QRCodeWriter;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.security.TotpAuthenticationProvider;
import io.github.susimsek.springbootjweauthjpademo.security.TotpUtil;
import io.github.susimsek.springbootjweauthjpademo.security.UserPrincipalService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class SecurityTotpConfig {

    private final ApplicationProperties props;

    @Bean
    public QRCodeWriter qrCodeWriter() {
        return new QRCodeWriter();
    }

    @Bean
    public TotpUtil totpUtil() {
        return new TotpUtil(props);
    }

    @Bean
    public TotpAuthenticationProvider totpAuthenticationProvider(
        UserPrincipalService userDetailsService,
        TotpUtil totpUtil
    ) {
        return new TotpAuthenticationProvider(
            userDetailsService, totpUtil);
    }
}
