package com.rps.samaj.auth.dto;

import com.rps.samaj.auth.entity.User;

import java.util.Map;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String phone,
        String googleId,
        boolean emailVerified,
        boolean phoneVerified,
        String status,
        String role,
        Map<String, Object> metadata
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getPhone(),
                user.getGoogleId(),
                user.isEmailVerified(),
                user.isPhoneVerified(),
                user.getStatus().name(),
                user.getRole().name(),
                user.getMetadata() != null ? user.getMetadata() : Map.of()
        );
    }
}
