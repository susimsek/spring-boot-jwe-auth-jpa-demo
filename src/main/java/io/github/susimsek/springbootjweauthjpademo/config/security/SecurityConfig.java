package io.github.susimsek.springbootjweauthjpademo.config.security;

import io.github.susimsek.springbootjweauthjpademo.exception.ProblemSupport;
import io.github.susimsek.springbootjweauthjpademo.mapper.UserMapper;
import io.github.susimsek.springbootjweauthjpademo.security.DomainAuthenticationEventPublisher;
import io.github.susimsek.springbootjweauthjpademo.security.DomainUserDetailsService;
import io.github.susimsek.springbootjweauthjpademo.security.TotpAuthenticationProvider;
import io.github.susimsek.springbootjweauthjpademo.security.UserPrincipalService;
import io.github.susimsek.springbootjweauthjpademo.service.AccountLockService;
import io.github.susimsek.springbootjweauthjpademo.service.UserLookupService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration(proxyBeanMethods = false)
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   MvcRequestMatcher.Builder mvc,
                                                   ProblemSupport problemSupport) throws Exception {
        http
            .cors(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .headers(headers -> headers
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
            )
            .authorizeHttpRequests(authz ->
                authz
                    .requestMatchers(
                        "/login",
                        "/register",
                        "/verify-email",
                        "/confirm-email",
                        "/forgot-password",
                        "/reset-password",
                        "/403",
                        "/css/**", "/js/**", "/webjars/**",
                        "/images/**",
                        "/favicon.ico"
                    ).permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**"
                    ).permitAll()
                    .requestMatchers(antMatcher("/h2-console/**")).permitAll()
                    .requestMatchers(mvc.pattern("/api/auth/register")).permitAll()
                    .requestMatchers(mvc.pattern("/api/auth/verify-email")).permitAll()
                    .requestMatchers(mvc.pattern("/api/account/confirm-email")).permitAll()
                    .requestMatchers(mvc.pattern("/api/auth/login")).permitAll()
                    .requestMatchers(mvc.pattern("/api/auth/forgot-password")).permitAll()
                    .requestMatchers(mvc.pattern("/api/auth/reset-password")).permitAll()
                    .requestMatchers(mvc.pattern("/api/qrcode")).authenticated()
                    .requestMatchers(mvc.pattern("/api/auth/refresh-token"))
                    .hasAuthority("ROLE_REFRESH")
                    .requestMatchers(mvc.pattern("/api/auth/setup-totp"))
                    .hasAuthority("ROLE_MFA")
                    .requestMatchers(mvc.pattern("/api/auth/confirm-totp"))
                    .hasAuthority("ROLE_MFA")
                    .requestMatchers(mvc.pattern("/api/auth/verify-totp"))
                    .hasAuthority("ROLE_MFA")
                    .requestMatchers(mvc.pattern("/api/hello/admin"))
                    .hasAuthority("ROLE_ADMIN")
                    .requestMatchers(mvc.pattern("/api/admin/**")).hasAuthority("ROLE_ADMIN")
                    .requestMatchers(mvc.pattern("/api/**")).hasAuthority("ROLE_ACCESS")
                    .anyRequest().authenticated()
            )
            .formLogin(AbstractHttpConfigurer::disable
            )
            .logout(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions
                .defaultAuthenticationEntryPointFor(
                    new LoginUrlAuthenticationEntryPoint("/login"),
                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                )
                .accessDeniedPage("/403")
                .accessDeniedHandler(problemSupport)
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(withDefaults())
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
            );

        return http.build();
    }

    @Bean
    public UserPrincipalService userDetailsService(UserLookupService userLookupService,
                                                   UserMapper userMapper) {
        return new DomainUserDetailsService(userLookupService, userMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
        DaoAuthenticationProvider daoAuthenticationProvider,
        ObjectProvider<TotpAuthenticationProvider> totpAuthProvider,
        AuthenticationEventPublisher authenticationEventPublisher
    ) {
        List<AuthenticationProvider> providers = new ArrayList<>();
        providers.add(daoAuthenticationProvider);

        TotpAuthenticationProvider totpAuth = totpAuthProvider.getIfAvailable();
        if (totpAuth != null) {
            providers.add(totpAuth);
        }

        ProviderManager authManager = new ProviderManager(providers);
        authManager.setAuthenticationEventPublisher(authenticationEventPublisher);
        return authManager;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(
        UserPrincipalService userDetailsService,
        PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationEventPublisher authenticationEventPublisher(AccountLockService accountLockService) {
        return new DomainAuthenticationEventPublisher(accountLockService);
    }

    @Bean
    public MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }

}
