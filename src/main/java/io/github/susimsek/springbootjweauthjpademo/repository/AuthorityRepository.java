package io.github.susimsek.springbootjweauthjpademo.repository;

import io.github.susimsek.springbootjweauthjpademo.domain.Authority;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String>, JpaSpecificationExecutor<Authority> {

    String AUTHORITIES_BY_NAME_CACHE = "authoritiesByName";


    @Cacheable(cacheNames = AUTHORITIES_BY_NAME_CACHE, unless = "#result == null")
    Optional<Authority> findByName(String name);
    boolean existsByName(String name);
}
