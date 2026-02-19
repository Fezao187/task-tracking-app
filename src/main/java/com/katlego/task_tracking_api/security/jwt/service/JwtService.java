package com.katlego.task_tracking_api.security.jwt.service;

import com.katlego.task_tracking_api.security.entity.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {
    private final String secretKey;
    private final int accessTokenExpirationMs;
    private final int refreshTokenExpirationMs;

    public JwtService(@Value("${app.jwt.secret}") String jwtSecretKey,
                      @Value("${app.jwt.access-token-expiration-ms}") int accessTokenExpirationMs,
                      @Value("${app.jwt.refresh-token-expiration-ms}") int refreshTokenExpirationMs) {
        this.secretKey = jwtSecretKey;
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(String email, Map<String, Object> claims) {
        return generateToken(email, claims, accessTokenExpirationMs, TokenType.ACCESS);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, Map.of(), refreshTokenExpirationMs, TokenType.REFRESH);
    }

    private String generateToken(String email, Map<String, Object> claims, int expirationMs, TokenType tokenType) {
        Instant now = Instant.now();
        Map<String, Object> tokenClaims = new HashMap<>(claims);
        tokenClaims.put("tokenType", tokenType.name());

        return Jwts.builder()
                .subject(email)
                .issuedAt(Date.from(now))
                .claims(tokenClaims)
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(getSigningKey())
                .compact();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public TokenType extractTokenType(String token) {
        String tokenType = extractClaim(token, claims -> claims.get("tokenType", String.class));
        return TokenType.valueOf(tokenType);
    }

    public Boolean validateAccessToken(String token, UserDetails userDetails) {
        return validateToken(token, userDetails, TokenType.ACCESS);
    }

    public Boolean validateRefreshToken(String token, UserDetails userDetails) {
        return validateToken(token, userDetails, TokenType.REFRESH);
    }

    private Boolean validateToken(String token, UserDetails userDetails, TokenType expectedType) {
        try {
            final String username = extractUsername(token);
            final TokenType tokenType = extractTokenType(token);

            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(token)
                    && tokenType == expectedType;
        } catch (Exception e) {
            log.error("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}