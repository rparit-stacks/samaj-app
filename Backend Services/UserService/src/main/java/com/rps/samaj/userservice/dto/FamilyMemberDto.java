package com.rps.samaj.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record FamilyMemberDto(
        String id,
        @NotBlank(message = "Name is required")
        String name,
        @NotBlank(message = "Relation is required")
        String relation,
        String city,
        String phone,
        String email
) {
}

