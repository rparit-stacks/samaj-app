package com.rps.samaj.auth.repository;

import com.rps.samaj.auth.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findByRefreshTokenAndRevokedFalse(String refreshToken);
    List<Session> findByUserIdAndRevokedFalse(UUID userId);
    boolean existsByRefreshTokenAndRevokedFalse(String refreshToken);

    @Modifying
    @Query("UPDATE Session s SET s.revoked = true WHERE s.refreshToken = ?1")
    void revokeByRefreshToken(String refreshToken);

    @Modifying
    @Query("UPDATE Session s SET s.revoked = true WHERE s.userId = ?1")
    void revokeAllByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM Session s WHERE s.expiresAt < ?1")
    int deleteExpiredSessions(Instant before);
}
