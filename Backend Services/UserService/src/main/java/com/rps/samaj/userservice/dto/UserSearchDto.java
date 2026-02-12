package com.rps.samaj.userservice.dto;

import java.util.UUID;

public record UserSearchDto(
        UUID userId,
        String fullName,
        String city,
        String profession,
        String avatarUrl
) {
}

