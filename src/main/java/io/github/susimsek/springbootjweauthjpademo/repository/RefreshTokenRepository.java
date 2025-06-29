package io.github.susimsek.springbootjweauthjpademo.repository;

import io.github.susimsek.springbootjweauthjpademo.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

    @Modifying
    @Transactional
    void deleteByUserId(String userId);
}
