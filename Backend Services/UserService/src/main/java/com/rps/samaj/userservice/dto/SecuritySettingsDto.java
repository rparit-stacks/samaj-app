package com.rps.samaj.userservice.dto;

public record SecuritySettingsDto(
        boolean twoFactorEnabled,
        boolean loginAlertsEnabled
) {
}

