package com.rps.samaj.auth.service;

import com.rps.samaj.auth.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${app.jwt.secret:samaj-auth-secret-key-min-256-bits-for-hs256-algorithm}")
    private String secret;

    @Value("${app.jwt.access-expiry-minutes:60}")
    private long accessExpiryMinutes;

    @Value("${app.jwt.refresh-expiry-days:7}")
    private long refreshExpiryDays;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = new byte[32];
            System.arraycopy(secret.getBytes(StandardCharsets.UTF_8), 0, keyBytes, 0,
                    Math.min(secret.length(), 32));
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessExpiryMinutes * 60);
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(refreshExpiryDays * 24 * 60 * 60);
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(getSigningKey())
                .compact();
    }

    public Optional<UUID> validateAndGetUserId(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build();
            Claims claims = parser.parseSignedClaims(token).getPayload();
            return Optional.of(UUID.fromString(claims.getSubject()));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public boolean isValidRefreshToken(String token) {
        try {
            JwtParser parser = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build();
            Claims claims = parser.parseSignedClaims(token).getPayload();
            return "refresh".equals(claims.get("type"));
        } catch (JwtException e) {
            return false;
        }
    }

    public long getAccessExpirySeconds() {
        return accessExpiryMinutes * 60;
    }

    public Instant getAccessExpiryInstant() {
        return Instant.now().plusSeconds(accessExpiryMinutes * 60);
    }

    public Instant getRefreshExpiryInstant() {
        return Instant.now().plusSeconds(refreshExpiryDays * 24 * 60 * 60);
    }
}
