package com.rps.samaj.emergency.repository;

import com.rps.samaj.emergency.entity.Emergency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface EmergencyRepository extends JpaRepository<Emergency, Long> {

    List<Emergency> findByCreatorUserIdOrderByCreatedAtDesc(UUID creatorUserId);

    // All non-cancelled emergencies, newest first
    List<Emergency> findByStatusNotOrderByCreatedAtDesc(Emergency.Status status);

    // Count by creator and status
    long countByCreatorUserId(UUID creatorUserId);

    long countByCreatorUserIdAndStatus(UUID creatorUserId, Emergency.Status status);

    @Query("SELECT COALESCE(SUM(e.contactClickCount), 0) FROM Emergency e WHERE e.creatorUserId = :userId")
    long sumContactClicksByCreator(@Param("userId") UUID userId);

    @Query("SELECT COALESCE(SUM(e.viewCount), 0) FROM Emergency e WHERE e.creatorUserId = :userId")
    long sumViewsByCreator(@Param("userId") UUID userId);
}
