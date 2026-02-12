package com.rps.samaj.auth.dto;

import java.util.Map;

public record UpdateProfileRequest(
        String name,
        String phone,
        Map<String, Object> metadata
) {}
