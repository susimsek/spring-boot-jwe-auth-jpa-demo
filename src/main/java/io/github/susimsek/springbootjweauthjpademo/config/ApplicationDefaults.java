package io.github.susimsek.springbootjweauthjpademo.config;

import io.github.susimsek.springbootjweauthjpademo.config.logging.HttpLogLevel;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.Set;
import java.util.List;

@SuppressWarnings({"java:S115", "java:S1214"})
public interface ApplicationDefaults {

    interface Liquibase {
        boolean asyncStart = true;
    }

    interface Cache {
        interface Caffeine {
            Duration timeToLiveDuration = Duration.ofHours(1);

            int initialCapacity = 500;

            long maximumSize = 1000L;
        }
    }

    interface Security {
        interface Encryption {
            String base64Secret = null;
        }
        interface Jwt {
            boolean jweEnabled = false;
            Duration expirationDuration = Duration.ofSeconds(3600);
            Duration mfaExpirationDuration = Duration.ofSeconds(300);
            Duration refreshExpirationDuration = Duration.ofDays(7);
        }
        interface Lock {
            int maxFailedAttempts = 5;
            Duration lockDuration = Duration.ofMinutes(15);
        }
    }

    interface ApiDocs {
        String title = "Application API";
        String description = "API documentation";
        String version = "0.0.1";
        String termsOfServiceUrl = null;
        String contactName = "Şuayb Şimşek";
        String contactUrl = "https://github.com/susimsek";
        String contactEmail = "contact@susimsek.dev";
        String license = "Apache 2.0";
        String licenseUrl = "http://springdoc.org";
    }

    interface Redis {
        interface Embedded {
            boolean enabled = false;
            int port = 6379;
        }
    }

    interface Database {
        interface H2 {
            interface Embedded {
                int port = 9092;
            }
        }
    }

    interface RateLimiter {
        boolean filterEnabled = true;

        interface Config {
            long limitForPeriod = 5;
            java.time.Duration limitRefreshPeriod = java.time.Duration.ofHours(1);
        }
    }

    interface Logging {
        HttpLogLevel level = HttpLogLevel.BASIC;
        DataSize maxPayloadSize = DataSize.ofKilobytes(16);
        boolean filterEnabled = true;
        boolean aspectEnabled = false;
        boolean clientInterceptorEnabled = true;
        List<String> serverExcludeUriPatterns = List.of(
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/css/**",
            "/js/**",
            "/webjars/**",
            "/images/**",
            "/favicon.ico"
        );

        List<String> clientExcludeUriPatterns = List.of();

        interface Obfuscate {
            Set<String> headers = Set.of("Authorization");
            Set<String> cookies = Set.of("accessToken", "mfaToken", "refreshToken");
            Set<String> parameters = Set.of("token", "access_token");
            Set<String> paths = Set.of();
            Set<String> jsonBodyFields = Set.of("$.password", "*.token");
            String replacement = "***";
        }

        interface Loki {
            boolean enabled = false;
            String url = "http://localhost:3100/loki/api/v1/push";

            interface Batch {
                int maxItems = 1000;
                Duration timeout = Duration.ofSeconds(10);
            }
        }
    }
}
