package io.github.susimsek.springbootjweauthjpademo.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class VersionedEntity extends BaseEntity {

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
