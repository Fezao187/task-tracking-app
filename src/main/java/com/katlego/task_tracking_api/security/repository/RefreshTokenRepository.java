package com.katlego.task_tracking_api.security.repository;

import com.katlego.task_tracking_api.security.entity.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByEmailAndToken(String email, String token);

    void deleteByEmail(String email);

    void deleteByEmailAndToken(String email, String token);

    int deleteByExpiresAtBefore(Instant expirationTime);
}