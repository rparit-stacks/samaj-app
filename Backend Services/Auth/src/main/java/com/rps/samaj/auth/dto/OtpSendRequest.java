package com.rps.samaj.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OtpSendRequest(
        @NotBlank(message = "Email or phone is required") String identifier,

        @Pattern(regexp = "^(EMAIL|PHONE)$", message = "Type must be EMAIL or PHONE")
        String type,

        @Pattern(regexp = "^(REGISTRATION|LOGIN|PASSWORD_RESET|PHONE_VERIFICATION|EMAIL_VERIFICATION)$",
                message = "Invalid purpose")
        String purpose
) {}
