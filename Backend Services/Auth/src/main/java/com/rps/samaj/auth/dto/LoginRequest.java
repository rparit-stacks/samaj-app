package com.rps.samaj.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Email or phone is required") String identifier,
        @NotBlank(message = "Password is required") String password
) {}
