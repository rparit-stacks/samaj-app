package com.rps.samaj.userservice.dto;

public record PrivacySettingsDto(
        boolean showEmail,
        boolean showBloodGroup,
        String profileVisibility
) {
}

