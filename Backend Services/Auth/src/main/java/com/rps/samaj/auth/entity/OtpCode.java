package com.rps.samaj.auth.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "otp_codes", indexes = {
        @Index(name = "idx_otp_identifier", columnList = "identifier"),
        @Index(name = "idx_otp_expiry", columnList = "expires_at")
})
public class OtpCode {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String identifier; // email or phone

    @Enumerated(EnumType.STRING)
    @Column(name = "identifier_type", nullable = false)
    private IdentifierType identifierType;

    @Column(nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OtpPurpose purpose;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public enum IdentifierType {
        EMAIL, PHONE
    }

    public enum OtpPurpose {
        REGISTRATION, LOGIN, PASSWORD_RESET, PHONE_VERIFICATION, EMAIL_VERIFICATION
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }
    public IdentifierType getIdentifierType() { return identifierType; }
    public void setIdentifierType(IdentifierType identifierType) { this.identifierType = identifierType; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public OtpPurpose getPurpose() { return purpose; }
    public void setPurpose(OtpPurpose purpose) { this.purpose = purpose; }
    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }
    public void incrementAttempts() { this.attempts++; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
