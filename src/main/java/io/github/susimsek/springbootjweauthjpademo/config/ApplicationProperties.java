package io.github.susimsek.springbootjweauthjpademo.config;

import io.github.susimsek.springbootjweauthjpademo.config.logging.HttpLogLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
@Data
public class ApplicationProperties {

    private final Liquibase liquibase = new Liquibase();
    private final Mail mail = new Mail();
    private final Cache cache = new Cache();
    private final Security security = new Security();
    private final ApiDocs apiDocs = new ApiDocs();
    private final Redis redis = new Redis();
    private final RateLimiter rateLimiter = new RateLimiter();
    private final Logging logging = new Logging();
    private final Database database = new Database();

    @Getter @Setter
    public static class Database {
        private final H2 h2 = new H2();

        @Getter
        @Setter
        public static class H2 {
            private final Embedded embedded = new Embedded();

            @Getter
            @Setter
            public static class Embedded {
                private int port = ApplicationDefaults.Database.H2.Embedded.port;
            }
        }
    }

    @Getter
    @Setter
    public static class Liquibase {
        private Boolean asyncStart = ApplicationDefaults.Liquibase.asyncStart;
    }

    @Getter
    @Setter
    public static class Mail {
        private String from;
        private String baseUrl;
    }

    @Getter
    @Setter
    public static class Cache {
        private final Caffeine caffeine = new Caffeine();

        @Getter
        @Setter
        public static class Caffeine {
            private final CacheConfig defaultConfig = new CacheConfig();
            private Map<String, CacheConfig> caches = new HashMap<>();

            @Getter
            @Setter
            public static class CacheConfig {
                private Duration ttl = ApplicationDefaults.Cache.Caffeine.timeToLiveDuration;
                private Integer initialCapacity = ApplicationDefaults.Cache.Caffeine.initialCapacity;
                private Long maximumSize = ApplicationDefaults.Cache.Caffeine.maximumSize;
            }
        }
    }

    @Getter
    @Setter
    public static class Security {
        private final Jwt jwt = new Jwt();
        private final Mfa mfa = new Mfa();
        private final Encryption encryption = new Encryption();
        private final Lock lock = new Lock();

        @Getter
        @Setter
        public static class Encryption {
            private String base64Secret =  ApplicationDefaults.Security.Encryption.base64Secret;
        }

        @Getter
        @Setter
        public static class Jwt {
            private boolean jweEnabled = ApplicationDefaults.Security.Jwt.jweEnabled;
            private Pair signing;
            private Pair encryption;
            private String issuer;
            private Duration expirationDuration = ApplicationDefaults.Security.Jwt.expirationDuration;
            private Duration mfaExpirationDuration = ApplicationDefaults.Security.Jwt.mfaExpirationDuration;
            private Duration refreshExpirationDuration = ApplicationDefaults.Security.Jwt.refreshExpirationDuration;


            @Getter
            @Setter
            public static class Pair {
                private String publicKey;
                private String privateKey;
                private String keyId;
            }
        }

        @Getter
        @Setter
        public static class Mfa {
            private String issuer;
        }

        @Getter
        @Setter
        public static class Lock {
            private int maxFailedAttempts = ApplicationDefaults.Security.Lock.maxFailedAttempts;
            private Duration lockDuration = ApplicationDefaults.Security.Lock.lockDuration;
        }
    }

    @Getter
    @Setter
    public static class ApiDocs {
        private String title = ApplicationDefaults.ApiDocs.title;
        private String description = ApplicationDefaults.ApiDocs.description;
        private String version = ApplicationDefaults.ApiDocs.version;
        private String termsOfServiceUrl = ApplicationDefaults.ApiDocs.termsOfServiceUrl;
        private String contactName = ApplicationDefaults.ApiDocs.contactName;
        private String contactUrl = ApplicationDefaults.ApiDocs.contactUrl;
        private String contactEmail = ApplicationDefaults.ApiDocs.contactEmail;
        private String license = ApplicationDefaults.ApiDocs.license;
        private String licenseUrl = ApplicationDefaults.ApiDocs.licenseUrl;
        private Server[] servers = {};

        @Getter
        @Setter
        public static class Server {
            private String url;
            private String description;
        }
    }

    @Getter
    @Setter
    public static class Redis {
      private final Embedded embedded = new Embedded();

      @Getter
      @Setter
      public static class Embedded {
        private boolean enabled = ApplicationDefaults.Redis.Embedded.enabled;
        private int port = ApplicationDefaults.Redis.Embedded.port;
      }
    }

    @Getter
    @Setter
    public static class RateLimiter {
        private final Config defaultConfig = new Config();
        private Map<String, Config> instances = new HashMap<>();
        private boolean filterEnabled = ApplicationDefaults.RateLimiter.filterEnabled;

        @Getter
        @Setter
        public static class Config {
            private long limitForPeriod = ApplicationDefaults.RateLimiter.Config.limitForPeriod;
            private Duration limitRefreshPeriod = ApplicationDefaults.RateLimiter.Config.limitRefreshPeriod;
        }
    }

    @Getter
    @Setter
    public static class Logging {
        private HttpLogLevel level = ApplicationDefaults.Logging.level;
        private DataSize maxPayloadSize = ApplicationDefaults.Logging.maxPayloadSize;
        private boolean filterEnabled = ApplicationDefaults.Logging.filterEnabled;
        private boolean aspectEnabled = ApplicationDefaults.Logging.aspectEnabled;
        private boolean clientInterceptorEnabled = ApplicationDefaults.Logging.clientInterceptorEnabled;
        private final Obfuscate obfuscate = new Obfuscate();
        private final Loki loki = new Loki();

        @Getter
        @Setter
        public static class Obfuscate {
            private Set<String> headers = ApplicationDefaults.Logging.Obfuscate.headers;
            private Set<String> cookies = ApplicationDefaults.Logging.Obfuscate.cookies;
            private Set<String> parameters = ApplicationDefaults.Logging.Obfuscate.parameters;
            private Set<String> paths = ApplicationDefaults.Logging.Obfuscate.paths;
            private Set<String> jsonBodyFields = ApplicationDefaults.Logging.Obfuscate.jsonBodyFields;
            private String replacement = ApplicationDefaults.Logging.Obfuscate.replacement;
        }

        @Getter
        @Setter
        public static class Loki {
            private boolean enabled = ApplicationDefaults.Logging.Loki.enabled;
            private String url = ApplicationDefaults.Logging.Loki.url;
        }
    }

}
