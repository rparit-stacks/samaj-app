package com.rps.samaj.emergency.repository;

import com.rps.samaj.emergency.entity.Emergency;
import com.rps.samaj.emergency.entity.EmergencyHelp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmergencyHelpRepository extends JpaRepository<EmergencyHelp, Long> {

    List<EmergencyHelp> findByEmergencyOrderByHelpedAtAsc(Emergency emergency);

    Optional<EmergencyHelp> findByEmergencyAndHelperUserId(Emergency emergency, UUID helperUserId);

    long countByHelperUserId(UUID helperUserId);

    @Query("select count(distinct eh.emergency.creatorUserId) from EmergencyHelp eh where eh.helperUserId = :helperUserId")
    long countDistinctPeopleHelpedByHelperUserId(UUID helperUserId);

    List<EmergencyHelp> findByHelperUserIdOrderByHelpedAtDesc(UUID helperUserId);

    long countByEmergency(Emergency emergency);
}

