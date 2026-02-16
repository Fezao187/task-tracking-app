package com.katlego.task_tracking_api.security.service;

import com.katlego.task_tracking_api.security.entity.RefreshToken;
import com.katlego.task_tracking_api.security.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final int refreshTokenExpirationMs;

    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository,
                               @Value("${app.jwt.refresh-token-expiration-ms}") int refreshTokenExpirationMs) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public void saveRefreshToken(String email, String token) {
        refreshTokenRepository.deleteByEmail(email);

        Instant now = Instant.now();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setEmail(email);
        refreshToken.setToken(token);
        refreshToken.setCreatedAt(now);
        refreshToken.setExpiresAt(now.plusMillis(refreshTokenExpirationMs));

        refreshTokenRepository.save(refreshToken);
    }

    public boolean isRefreshTokenValid(String email, String token) {
        Optional<RefreshToken> refreshToken =
                refreshTokenRepository.findByEmailAndToken(email, token);

        return refreshToken.isPresent() && !refreshToken.get().isExpired();
    }

    public void rotateRefreshToken(String email, String oldToken, String newToken) {
        refreshTokenRepository.deleteByEmailAndToken(email, oldToken);
        saveRefreshToken(email, newToken);
    }

    public void deleteRefreshToken(String email) {
        refreshTokenRepository.deleteByEmail(email);
    }

    public int deleteExpiredTokens() {
        return refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
    }
}