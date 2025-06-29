package io.github.susimsek.springbootjweauthjpademo.repository;

import io.github.susimsek.springbootjweauthjpademo.domain.UserAuthorityMapping;
import io.github.susimsek.springbootjweauthjpademo.domain.UserAuthorityMappingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthorityMappingRepository
    extends JpaRepository<UserAuthorityMapping, UserAuthorityMappingId> {

    long countByAuthorityId(String authorityId);
}
