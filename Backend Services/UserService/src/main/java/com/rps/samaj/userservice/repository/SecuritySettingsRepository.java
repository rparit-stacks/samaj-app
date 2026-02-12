package com.rps.samaj.userservice.repository;

import com.rps.samaj.userservice.entity.SecuritySettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SecuritySettingsRepository extends JpaRepository<SecuritySettings, UUID> {
}

