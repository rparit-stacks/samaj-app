package com.rps.samaj.userservice.dto;

public record UserSettingsDto(
        boolean showPhone,
        boolean showInDirectory,
        boolean emergencyAlerts
) {
}

