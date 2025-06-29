package io.github.susimsek.springbootjweauthjpademo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Entity
@Table(name = "user_authority_mapping")
@IdClass(UserAuthorityMappingId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class UserAuthorityMapping extends BaseEntity {

    @Id
    @Column(name = "user_id", length = 36, nullable = false)
    private String userId;

    @Id
    @Column(name = "authority_id", length = 36, nullable = false)
    private String authorityId;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @MapsId("authorityId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "authority_id", insertable = false, updatable = false)
    private Authority authority;

    @Override
    public final boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UserAuthorityMapping other)) return false;
        Class<?> objCls = (obj instanceof HibernateProxy hp
            ? hp.getHibernateLazyInitializer().getPersistentClass()
            : obj.getClass());
        Class<?> thisCls = (this instanceof HibernateProxy hp
            ? hp.getHibernateLazyInitializer().getPersistentClass()
            : this.getClass());
        if (!thisCls.equals(objCls)) return false;
        return userId != null && userId.equals(other.userId)
            && authorityId != null && authorityId.equals(other.authorityId);
    }

    @Override
    public final int hashCode() {
        return (this instanceof HibernateProxy hp
            ? hp.getHibernateLazyInitializer().getPersistentClass().hashCode()
            : Objects.hash(userId, authorityId));
    }
}
