package io.github.susimsek.springbootjweauthjpademo.service;

import io.github.susimsek.springbootjweauthjpademo.dto.response.RefreshTokenDTO;
import io.github.susimsek.springbootjweauthjpademo.dto.response.TokenDTO;
import io.github.susimsek.springbootjweauthjpademo.domain.RefreshToken;
import io.github.susimsek.springbootjweauthjpademo.repository.RefreshTokenRepository;
import io.github.susimsek.springbootjweauthjpademo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public RefreshTokenDTO rotateRefreshToken(Jwt jwt) {
        RefreshToken stored = refreshTokenRepository.findById(jwt.getId())
            .orElseThrow(() -> new JwtException("Invalid refresh token"));

        String subject = jwt.getSubject();
        if (!stored.getUserId().equals(subject)) {
            throw new JwtException("Refresh token subject mismatch");
        }

        refreshTokenRepository.delete(stored);

        TokenDTO newRefresh = createRefreshToken(subject);

        // 6) Return both subject and new token
        return new RefreshTokenDTO(subject, newRefresh);
    }


    public TokenDTO createRefreshToken(String userId) {
        String jti = UUID.randomUUID().toString();
        TokenDTO dto = jwtUtil.generateRefreshToken(userId, jti);
        // 2) Persist in DB
        RefreshToken entity = new RefreshToken();
        entity.setJti(jti);
        entity.setUserId(userId);
        entity.setExpiresAt(Instant.now().plusSeconds(dto.tokenExpiresIn()));
        refreshTokenRepository.save(entity);
        // 3) Return DTO
        return dto;
    }

    public void deleteByUserId(String userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
