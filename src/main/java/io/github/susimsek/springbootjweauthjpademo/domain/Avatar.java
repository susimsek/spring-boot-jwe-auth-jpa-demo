package io.github.susimsek.springbootjweauthjpademo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.MapsId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(name = "user_avatar")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Avatar extends VersionedEntity {

    @Id
    @Column(name = "user_id", length = 36, nullable = false, updatable = false)
    private String userId;

    @Column(name = "content_type", length = 100, nullable = false)
    private String contentType;

    @Lob
    @Column(name = "content", nullable = false)
    private byte[] content;

    @Column(name = "content_hash", length = 64, nullable = false)
    private String contentHash;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Avatar other)) {
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
        return userId != null && Objects.equals(userId, other.userId);
    }

    @Override
    public int hashCode() {
        return this instanceof HibernateProxy proxy
            ? proxy.getHibernateLazyInitializer().getPersistentClass().hashCode()
            : getClass().hashCode();
    }
}
