package io.github.susimsek.springbootjweauthjpademo.config.security;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.JWEDecryptionKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import io.github.susimsek.springbootjweauthjpademo.config.ApplicationProperties;
import io.github.susimsek.springbootjweauthjpademo.security.CookieBearerTokenResolver;
import io.github.susimsek.springbootjweauthjpademo.security.KeyUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static io.github.susimsek.springbootjweauthjpademo.security.SecurityUtils.AUTHORITIES_KEY;

@Configuration(proxyBeanMethods = false)
@RequiredArgsConstructor
public class SecurityJwtConfig {

    private final ApplicationProperties props;

    @Bean
    public RSAKey signingKey() throws Exception {
        return KeyUtils.buildRsaKey(
            props.getSecurity().getJwt().getSigning().getPublicKey(),
            props.getSecurity().getJwt().getSigning().getPrivateKey(),
            props.getSecurity().getJwt().getSigning().getKeyId(),
            true
        );
    }

    @Bean
    @ConditionalOnProperty(prefix = "application.security.jwt", name = "jwe-enabled", havingValue = "true")
    public RSAKey encryptionKey() throws Exception {
        return KeyUtils.buildRsaKey(
            props.getSecurity().getJwt().getEncryption().getPublicKey(),
            props.getSecurity().getJwt().getEncryption().getPrivateKey(),
            props.getSecurity().getJwt().getEncryption().getKeyId(),
            false
        );
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(
        RSAKey signingKey,
        @Qualifier("encryptionKey") ObjectProvider<RSAKey> encryptionKeyProvider
    ) {
        List<JWK> keys = new ArrayList<>();
        keys.add(signingKey);
        Optional.ofNullable(encryptionKeyProvider.getIfAvailable())
            .ifPresent(keys::add);
        JWKSet jwkSet = new JWKSet(keys);
        return (jwkSelector, context) -> jwkSelector.select(jwkSet);
    }


    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        DefaultJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();

        if (props.getSecurity().getJwt().isJweEnabled()) {
            jwtProcessor.setJWEKeySelector(
                new JWEDecryptionKeySelector<>(
                    JWEAlgorithm.RSA_OAEP_256,
                    EncryptionMethod.A128GCM,
                    jwkSource
                )
            );
        }

        jwtProcessor.setJWSKeySelector(
            new JWSVerificationKeySelector<>(
                JWSAlgorithm.RS256,
                jwkSource
            )
        );
        jwtProcessor.setJWTClaimsSetVerifier((claims, context) -> {});

        return new NimbusJwtDecoder(jwtProcessor);
    }

    // JwtConfig.java
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("");
        authoritiesConverter.setAuthoritiesClaimName(AUTHORITIES_KEY);

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = authoritiesConverter.convert(jwt);

            String tokenUse = jwt.getClaimAsString("token_use");
            if ("mfa".equals(tokenUse)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_MFA"));
            } else if ("access".equals(tokenUse)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_ACCESS"));
            } else if ("refresh".equals(tokenUse)) {
                authorities.add(new SimpleGrantedAuthority("ROLE_REFRESH"));
            }
            return authorities;
        });

        return converter;
    }

    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        CookieBearerTokenResolver resolver = new CookieBearerTokenResolver();
        resolver.setAllowUriQueryParameter(false);
        resolver.setAllowFormEncodedBodyParameter(false);
        resolver.setAllowCookie(true);
        return resolver;
    }
}
