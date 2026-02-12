package com.rps.samaj.notification.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${app.jwt.secret:samaj-auth-secret-key-min-256-bits-for-hs256-algorithm}")
    private String secret;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            keyBytes = new byte[32];
            System.arraycopy(secret.getBytes(StandardCharsets.UTF_8), 0, keyBytes, 0,
                    Math.min(secret.length(), 32));
        }
        return Keys.hmacShaKeyFor(keyBytes);
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
}

