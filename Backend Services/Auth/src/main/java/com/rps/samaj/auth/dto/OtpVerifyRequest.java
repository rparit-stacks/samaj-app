package com.rps.samaj.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OtpVerifyRequest(
        @NotBlank(message = "Email or phone is required") String identifier,

        @NotBlank(message = "OTP code is required")
        @Size(min = 6, max = 6, message = "OTP must be 6 digits")
        String code,

        String purpose
) {}
