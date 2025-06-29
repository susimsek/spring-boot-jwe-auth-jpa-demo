package io.github.susimsek.springbootjweauthjpademo.repository;

import io.github.susimsek.springbootjweauthjpademo.domain.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {

    String USERS_BY_USERNAME_CACHE = "usersByUsername";

    String USERS_BY_ID_CACHE = "usersById";

    @EntityGraph("User.withAuthorities")
    @Cacheable(cacheNames = USERS_BY_USERNAME_CACHE, unless = "#result == null")
    Optional<User> findOneWithAuthoritiesByUsername(String username);

    @EntityGraph("User.withAuthorities")
    @Cacheable(cacheNames = USERS_BY_ID_CACHE, unless = "#result == null")
    Optional<User> findOneWithAuthoritiesById(String id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @EntityGraph("User.withAuthorities")
    Optional<User> findOneWithAuthoritiesByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    Optional<User> findByEmailChangeToken(String token);


    @EntityGraph("User.withAuthorities")
    @NonNull
    Page<User> findAll(Specification<User> spec,
                       @NonNull Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.failedAttempt = 0, u.locked = false, u.lockTime = NULL, u.lockExpiresAt = NULL WHERE u.id = :id")
    void resetAttemptsById(@Param("id") String id);
}
