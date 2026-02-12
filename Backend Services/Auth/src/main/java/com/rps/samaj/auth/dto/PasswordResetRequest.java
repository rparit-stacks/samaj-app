package com.rps.samaj.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @NotBlank(message = "Email or phone is required") String identifier,

        @NotBlank(message = "OTP is required")
        @Size(min = 6, max = 6) String otp,

        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String newPassword
) {}
