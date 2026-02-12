package com.rps.samaj.auth.repository;

import com.rps.samaj.auth.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    Optional<OtpCode> findTopByIdentifierAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
            String identifier, OtpCode.OtpPurpose purpose);

    List<OtpCode> findByIdentifierAndPurpose(String identifier, OtpCode.OtpPurpose purpose);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < ?1")
    int deleteExpiredOtps(Instant before);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.identifier = ?1 AND o.purpose = ?2")
    void invalidateExistingOtps(String identifier, OtpCode.OtpPurpose purpose);
}
