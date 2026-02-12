package com.rps.samaj.notification.dto;

public record NotificationPreferenceDto(
        boolean emailEnabled,
        boolean inAppEnabled,
        boolean securityEmailEnabled
) {
}

