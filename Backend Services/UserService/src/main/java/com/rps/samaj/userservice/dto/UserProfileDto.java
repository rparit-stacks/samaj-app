package com.rps.samaj.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record UserProfileDto(
        String fullName,
        String city,
        String profession,
        String bio,
        String avatarUrl,
        String coverImageUrl
) {
}

