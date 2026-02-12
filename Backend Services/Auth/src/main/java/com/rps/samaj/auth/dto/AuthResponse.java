package com.rps.samaj.auth.dto;

import com.rps.samaj.auth.entity.User;

import java.util.Map;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, User user) {
        return new AuthResponse(
                accessToken,
                refreshToken,
                expiresIn,
                UserResponse.from(user)
        );
    }
}
