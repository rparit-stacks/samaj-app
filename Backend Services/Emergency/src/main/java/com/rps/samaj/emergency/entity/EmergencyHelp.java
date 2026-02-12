package com.rps.samaj.emergency.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "emergency_helpers",
       uniqueConstraints = @UniqueConstraint(columnNames = {"emergency_id", "helper_user_id"}))
public class EmergencyHelp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emergency_id", nullable = false)
    private Emergency emergency;

    @Column(name = "helper_user_id", nullable = false)
    private UUID helperUserId;

    @Column(name = "helped_at", nullable = false)
    private Instant helpedAt = Instant.now();

    @Column(length = 1000)
    private String note;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Emergency getEmergency() {
        return emergency;
    }

    public void setEmergency(Emergency emergency) {
        this.emergency = emergency;
    }

    public UUID getHelperUserId() {
        return helperUserId;
    }

    public void setHelperUserId(UUID helperUserId) {
        this.helperUserId = helperUserId;
    }

    public Instant getHelpedAt() {
        return helpedAt;
    }

    public void setHelpedAt(Instant helpedAt) {
        this.helpedAt = helpedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

