package com.rps.samaj.emergency.repository;

import com.rps.samaj.emergency.entity.EmergencyAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmergencyAuditLogRepository extends JpaRepository<EmergencyAuditLog, Long> {

    List<EmergencyAuditLog> findByEmergencyIdOrderByCreatedAtAsc(Long emergencyId);

    List<EmergencyAuditLog> findByActorUserIdOrderByCreatedAtDesc(UUID actorUserId);
}

