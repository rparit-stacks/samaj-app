package com.rps.samaj.userservice.repository;

import com.rps.samaj.userservice.entity.PrivacySettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PrivacySettingsRepository extends JpaRepository<PrivacySettings, UUID> {
}

