package com.rps.samaj.emergency.dto;

import com.rps.samaj.emergency.entity.Emergency;

import java.time.Instant;
import java.util.UUID;

public class EmergencyDtos {

    public record ContactPreferences(
            String phone,
            String whatsapp,
            String email,
            boolean allowPhone,
            boolean allowWhatsapp,
            boolean allowEmail
    ) {}

    public record EmergencyResponse(
            Long id,
            UUID creatorUserId,
            Emergency.Type type,
            String title,
            String description,
            String area,
            String city,
            String state,
            String country,
            String landmark,
            String locationDescription,
            Double latitude,
            Double longitude,
            Emergency.Status status,
            Instant emergencyAt,
            Instant createdAt,
            Instant updatedAt,
            long helperCount,
            long viewCount,
            long contactClickCount,
            boolean resolvedByExternal,
            String externalHelperNote,
            ContactPreferences contactPreferences
    ) {}

    public record CreateEmergencyRequest(
            String type,
            String title,
            String description,
            String area,
            String city,
            String state,
            String country,
            String landmark,
            String locationDescription,
            Double latitude,
            Double longitude,
            Instant emergencyAt,
            String contactPhone,
            String contactWhatsapp,
            String contactEmail,
            Boolean allowPhone,
            Boolean allowWhatsapp,
            Boolean allowEmail
    ) {}

    public record UpdateEmergencyRequest(
            String type,
            String title,
            String description,
            String area,
            String city,
            String state,
            String country,
            String landmark,
            String locationDescription,
            Double latitude,
            Double longitude,
            Instant emergencyAt,
            String contactPhone,
            String contactWhatsapp,
            String contactEmail,
            Boolean allowPhone,
            Boolean allowWhatsapp,
            Boolean allowEmail
    ) {}

    public record UpdateEmergencyStatusRequest(
            String status
    ) {}

    public record ResolveEmergencyRequest(
            UUID helperUserId,
            boolean externalHelper,
            String externalHelperNote,
            String note
    ) {}

    public record MarkHelperRequest(
            UUID helperUserId,
            String note
    ) {}

    public record EmergencyHelpResponse(
            Long emergencyId,
            UUID helperUserId,
            Instant helpedAt,
            String note
    ) {}

    public record HelperStatsResponse(
            UUID helperUserId,
            long totalHelps,
            long distinctPeopleHelped,
            Instant firstHelpAt,
            Instant lastHelpAt
    ) {}

    public record DashboardStatsResponse(
            long totalEmergenciesCreated,
            long activeEmergencies,
            long resolvedEmergencies,
            long totalContactClicks,
            long totalViews,
            long totalPeopleHelped
    ) {}
}
