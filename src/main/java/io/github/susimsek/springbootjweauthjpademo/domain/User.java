package io.github.susimsek.springbootjweauthjpademo.domain;

import io.github.susimsek.springbootjweauthjpademo.security.CryptoConverter;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedAttributeNode;
import jakarta.persistence.NamedEntityGraph;
import jakarta.persistence.NamedSubgraph;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.proxy.HibernateProxy;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "user_identity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedEntityGraph(
    name = "User.withAuthorities",
    attributeNodes = @NamedAttributeNode(value = "authorities", subgraph = "auth-subgraph"),
    subgraphs = @NamedSubgraph(
        name = "auth-subgraph",
        attributeNodes = @NamedAttributeNode("authority")
    )
)
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "username", length = 50, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "image_url", length = 256)
    private String imageUrl;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "locked", nullable = false)
    private boolean locked;

    @Column(name = "failed_attempt")
    private Integer failedAttempt;

    @Column(name = "lock_time")
    private Instant lockTime;

    @Column(name = "lock_expires_at")
    private Instant lockExpiresAt;

    @Column(name = "protected", nullable = false)
    private boolean protectedFlag;

    @Column(name = "mfa_enabled", nullable = false)
    private boolean mfaEnabled;

    @Column(name = "mfa_verified", nullable = false)
    private boolean mfaVerified = false;

    @Column(name = "mfa_secret", length = 100)
    @Convert(converter = CryptoConverter.class)
    private String mfaSecret;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "provider", length = 50)
    private String provider;

    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(name = "email_verification_token", length = 64)
    private String emailVerificationToken;

    @Column(name = "email_verification_expires_at")
    private Instant emailVerificationExpiresAt;

    @Column(name = "pending_email", length = 100)
    private String pendingEmail;

    @Column(name = "email_change_token", length = 64)
    private String emailChangeToken;

    @Column(name = "email_change_token_expires_at")
    private Instant emailChangeTokenExpiresAt;

    @Column(name = "password_reset_token", length = 64)
    private String passwordResetToken;

    @Column(name = "password_reset_expires_at")
    private Instant passwordResetExpiresAt;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY,
        cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private Set<UserAuthorityMapping> authorities = new HashSet<>();

    @OneToOne(
        mappedBy = "user",
        fetch = FetchType.LAZY,
        cascade = CascadeType.ALL,
        orphanRemoval = true)
    private Avatar avatar;

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof User other)) {
            return false;
        }
        Class<?> objClass = obj instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass()
            : obj.getClass();
        Class<?> thisClass = this instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass()
            : this.getClass();
        if (!thisClass.equals(objClass)) {
            return false;
        }
        return id != null && Objects.equals(id, other.id);
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }

    public void lock(Duration duration) {
        Instant now = Instant.now();
        setLockTime(now);
        setLockExpiresAt(now.plus(duration));
        setLocked(true);
    }

    public void unlock() {
        setFailedAttempt(0);
        setLocked(false);
        setLockTime(null);
        setLockExpiresAt(null);
    }
}
