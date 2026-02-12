package com.rps.samaj.emergency.service;

import com.rps.samaj.emergency.dto.EmergencyDtos;
import com.rps.samaj.emergency.entity.Emergency;
import com.rps.samaj.emergency.entity.EmergencyAuditLog;
import com.rps.samaj.emergency.entity.EmergencyHelp;
import com.rps.samaj.emergency.repository.EmergencyHelpRepository;
import com.rps.samaj.emergency.repository.EmergencyAuditLogRepository;
import com.rps.samaj.emergency.repository.EmergencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional
public class EmergencyService {

    private final EmergencyRepository emergencyRepository;
    private final EmergencyHelpRepository emergencyHelpRepository;
    private final EmergencyAuditLogRepository auditLogRepository;

    public EmergencyService(EmergencyRepository emergencyRepository,
                            EmergencyHelpRepository emergencyHelpRepository,
                            EmergencyAuditLogRepository auditLogRepository) {
        this.emergencyRepository = emergencyRepository;
        this.emergencyHelpRepository = emergencyHelpRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /* ── Create ───────────────────────────────────────────── */

    public EmergencyDtos.EmergencyResponse createEmergency(UUID creatorUserId,
                                                           EmergencyDtos.CreateEmergencyRequest request) {
        validateCreate(request);
        Emergency e = new Emergency();
        e.setCreatorUserId(creatorUserId);
        applyCommonFields(e, request.type(), request.title(), request.description(),
                request.area(), request.city(), request.state(), request.country(), request.landmark(),
                request.locationDescription(), request.latitude(), request.longitude(), request.emergencyAt(),
                request.contactPhone(), request.contactWhatsapp(), request.contactEmail(),
                request.allowPhone(), request.allowWhatsapp(), request.allowEmail());
        Emergency saved = emergencyRepository.save(e);
        logEvent(saved.getId(), creatorUserId, EmergencyAuditLog.EventType.CREATED,
                "{\"title\":\"" + escape(saved.getTitle()) + "\"}");
        return toResponse(saved);
    }

    /* ── Edit (creator only) ──────────────────────────────── */

    public EmergencyDtos.EmergencyResponse editEmergency(Long id, UUID userId,
                                                         EmergencyDtos.UpdateEmergencyRequest request) {
        Emergency e = findOrThrow(id);
        assertCreator(e, userId);
        if (e.getStatus() == Emergency.Status.RESOLVED || e.getStatus() == Emergency.Status.CLOSED) {
            throw new IllegalArgumentException("Cannot edit a resolved/closed emergency");
        }
        applyCommonFields(e, request.type(), request.title(), request.description(),
                request.area(), request.city(), request.state(), request.country(), request.landmark(),
                request.locationDescription(), request.latitude(), request.longitude(), request.emergencyAt(),
                request.contactPhone(), request.contactWhatsapp(), request.contactEmail(),
                request.allowPhone(), request.allowWhatsapp(), request.allowEmail());
        Emergency saved = emergencyRepository.save(e);
        logEvent(saved.getId(), userId, EmergencyAuditLog.EventType.STATUS_CHANGED,
                "{\"action\":\"EDITED\"}");
        return toResponse(saved);
    }

    /* ── Delete / Cancel (creator only) ───────────────────── */

    public void deleteEmergency(Long id, UUID userId) {
        Emergency e = findOrThrow(id);
        assertCreator(e, userId);
        e.setStatus(Emergency.Status.CANCELLED);
        emergencyRepository.save(e);
        logEvent(e.getId(), userId, EmergencyAuditLog.EventType.REMOVED,
                "{\"action\":\"DELETED_BY_CREATOR\"}");
    }

    /* ── Resolve with helper (creator only) ───────────────── */

    public EmergencyDtos.EmergencyResponse resolveEmergency(Long id, UUID creatorUserId,
                                                            EmergencyDtos.ResolveEmergencyRequest request) {
        Emergency e = findOrThrow(id);
        assertCreator(e, creatorUserId);
        if (e.getStatus() == Emergency.Status.RESOLVED || e.getStatus() == Emergency.Status.CLOSED) {
            throw new IllegalArgumentException("Already resolved/closed");
        }

        if (request.externalHelper()) {
            // Resolved by someone outside the platform
            e.setResolvedByExternal(true);
            e.setExternalHelperNote(request.externalHelperNote());
        } else {
            // Resolved by a platform user – record helper
            if (request.helperUserId() == null) {
                throw new IllegalArgumentException("helperUserId is required for platform helper");
            }
            EmergencyHelp help = emergencyHelpRepository
                    .findByEmergencyAndHelperUserId(e, request.helperUserId())
                    .orElseGet(EmergencyHelp::new);
            help.setEmergency(e);
            help.setHelperUserId(request.helperUserId());
            help.setNote(request.note());
            emergencyHelpRepository.save(help);
            logEvent(e.getId(), creatorUserId, EmergencyAuditLog.EventType.HELP_CONFIRMED,
                    "{\"helperUserId\":\"" + request.helperUserId() + "\"}");
        }

        e.setStatus(Emergency.Status.RESOLVED);
        Emergency saved = emergencyRepository.save(e);
        logEvent(saved.getId(), creatorUserId, EmergencyAuditLog.EventType.STATUS_CHANGED,
                "{\"oldStatus\":\"" + e.getStatus().name() + "\",\"newStatus\":\"RESOLVED\"" +
                        ",\"external\":" + request.externalHelper() + "}");
        return toResponse(saved);
    }

    /* ── Track view ───────────────────────────────────────── */

    public void trackView(Long id) {
        Emergency e = findOrThrow(id);
        e.incrementViewCount();
        emergencyRepository.save(e);
    }

    /* ── Track contact click ──────────────────────────────── */

    public void trackContactClick(Long id) {
        Emergency e = findOrThrow(id);
        e.incrementContactClickCount();
        emergencyRepository.save(e);
    }

    /* ── List ─────────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<EmergencyDtos.EmergencyResponse> listAll() {
        return emergencyRepository.findByStatusNotOrderByCreatedAtDesc(Emergency.Status.CANCELLED)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<EmergencyDtos.EmergencyResponse> listMyEmergencies(UUID userId) {
        return emergencyRepository.findByCreatorUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public EmergencyDtos.EmergencyResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /* ── Status update (generic) ──────────────────────────── */

    public EmergencyDtos.EmergencyResponse updateStatus(Long id, UUID userId,
                                                        EmergencyDtos.UpdateEmergencyStatusRequest request) {
        Emergency e = findOrThrow(id);
        assertCreator(e, userId);
        if (request.status() == null) throw new IllegalArgumentException("Status is required");
        Emergency.Status oldStatus = e.getStatus();
        Emergency.Status status = Emergency.Status.valueOf(request.status());
        e.setStatus(status);
        Emergency saved = emergencyRepository.save(e);
        logEvent(saved.getId(), userId, EmergencyAuditLog.EventType.STATUS_CHANGED,
                "{\"oldStatus\":\"" + oldStatus.name() + "\",\"newStatus\":\"" + status.name() + "\"}");
        return toResponse(saved);
    }

    /* ── Mark helper (legacy/direct) ──────────────────────── */

    public EmergencyDtos.EmergencyHelpResponse markHelper(Long emergencyId, UUID creatorUserId,
                                                          EmergencyDtos.MarkHelperRequest request) {
        Emergency e = findOrThrow(emergencyId);
        assertCreator(e, creatorUserId);
        if (request.helperUserId() == null) throw new IllegalArgumentException("helperUserId is required");
        EmergencyHelp help = emergencyHelpRepository
                .findByEmergencyAndHelperUserId(e, request.helperUserId())
                .orElseGet(EmergencyHelp::new);
        help.setEmergency(e);
        help.setHelperUserId(request.helperUserId());
        help.setNote(request.note());
        EmergencyHelp saved = emergencyHelpRepository.save(help);
        if (e.getStatus() == Emergency.Status.OPEN || e.getStatus() == Emergency.Status.IN_PROGRESS) {
            e.setStatus(Emergency.Status.HELP_RECEIVED);
            emergencyRepository.save(e);
        }
        logEvent(e.getId(), creatorUserId, EmergencyAuditLog.EventType.HELP_CONFIRMED,
                "{\"helperUserId\":\"" + saved.getHelperUserId() + "\"}");
        return new EmergencyDtos.EmergencyHelpResponse(
                e.getId(), saved.getHelperUserId(), saved.getHelpedAt(), saved.getNote());
    }

    @Transactional(readOnly = true)
    public List<EmergencyDtos.EmergencyHelpResponse> helpersForEmergency(Long emergencyId, UUID requesterUserId) {
        Emergency e = findOrThrow(emergencyId);
        return emergencyHelpRepository.findByEmergencyOrderByHelpedAtAsc(e).stream()
                .map(h -> new EmergencyDtos.EmergencyHelpResponse(
                        e.getId(), h.getHelperUserId(), h.getHelpedAt(), h.getNote()))
                .toList();
    }

    @Transactional(readOnly = true)
    public EmergencyDtos.HelperStatsResponse helperStats(UUID helperUserId) {
        long totalHelps = emergencyHelpRepository.countByHelperUserId(helperUserId);
        long distinctPeople = emergencyHelpRepository.countDistinctPeopleHelpedByHelperUserId(helperUserId);
        var helps = emergencyHelpRepository.findByHelperUserIdOrderByHelpedAtDesc(helperUserId);
        Instant first = helps.isEmpty() ? null : helps.get(helps.size() - 1).getHelpedAt();
        Instant last = helps.isEmpty() ? null : helps.get(0).getHelpedAt();
        return new EmergencyDtos.HelperStatsResponse(helperUserId, totalHelps, distinctPeople, first, last);
    }

    /* ── Dashboard stats (for logged-in user) ─────────────── */

    @Transactional(readOnly = true)
    public EmergencyDtos.DashboardStatsResponse dashboardStats(UUID userId) {
        long total = emergencyRepository.countByCreatorUserId(userId);
        long active = emergencyRepository.countByCreatorUserIdAndStatus(userId, Emergency.Status.OPEN)
                + emergencyRepository.countByCreatorUserIdAndStatus(userId, Emergency.Status.IN_PROGRESS)
                + emergencyRepository.countByCreatorUserIdAndStatus(userId, Emergency.Status.HELP_RECEIVED);
        long resolved = emergencyRepository.countByCreatorUserIdAndStatus(userId, Emergency.Status.RESOLVED);
        long contactClicks = emergencyRepository.sumContactClicksByCreator(userId);
        long views = emergencyRepository.sumViewsByCreator(userId);
        long peopleHelped = emergencyHelpRepository.countByHelperUserId(userId);
        return new EmergencyDtos.DashboardStatsResponse(
                total, active, resolved, contactClicks, views, peopleHelped);
    }

    /* ── Audit (admin) ────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<EmergencyAuditLog> getAuditForEmergency(Long emergencyId) {
        return auditLogRepository.findByEmergencyIdOrderByCreatedAtAsc(emergencyId);
    }

    @Transactional(readOnly = true)
    public List<EmergencyAuditLog> getAuditForActor(UUID actorUserId) {
        return auditLogRepository.findByActorUserIdOrderByCreatedAtDesc(actorUserId);
    }

    /* ── Private helpers ──────────────────────────────────── */

    private Emergency findOrThrow(Long id) {
        return emergencyRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Emergency not found"));
    }

    private void assertCreator(Emergency e, UUID userId) {
        if (!e.getCreatorUserId().equals(userId)) {
            throw new IllegalArgumentException("Only creator can perform this action");
        }
    }

    private void applyCommonFields(Emergency e,
                                   String type, String title, String description,
                                   String area, String city, String state, String country, String landmark,
                                   String locationDescription, Double latitude, Double longitude,
                                   Instant emergencyAt,
                                   String contactPhone, String contactWhatsapp, String contactEmail,
                                   Boolean allowPhone, Boolean allowWhatsapp, Boolean allowEmail) {
        if (type != null && !type.isBlank()) e.setType(Emergency.Type.valueOf(type));
        if (title != null && !title.isBlank()) e.setTitle(title.trim());
        if (description != null && !description.isBlank()) e.setDescription(description.trim());
        e.setArea(area);
        e.setCity(city);
        e.setState(state);
        e.setCountry(country);
        e.setLandmark(landmark);
        e.setLocationDescription(locationDescription);
        e.setLatitude(latitude);
        e.setLongitude(longitude);
        if (emergencyAt != null) e.setEmergencyAt(emergencyAt);
        e.setContactPhone(contactPhone);
        e.setContactWhatsapp(contactWhatsapp);
        e.setContactEmail(contactEmail);
        e.setAllowPhone(Boolean.TRUE.equals(allowPhone));
        e.setAllowWhatsapp(Boolean.TRUE.equals(allowWhatsapp));
        e.setAllowEmail(Boolean.TRUE.equals(allowEmail));
    }

    private void validateCreate(EmergencyDtos.CreateEmergencyRequest request) {
        if (request.title() == null || request.title().isBlank())
            throw new IllegalArgumentException("Title is required");
        if (request.description() == null || request.description().isBlank())
            throw new IllegalArgumentException("Description is required");
    }

    EmergencyDtos.EmergencyResponse toResponse(Emergency e) {
        EmergencyDtos.ContactPreferences contact = new EmergencyDtos.ContactPreferences(
                e.isAllowPhone() ? e.getContactPhone() : null,
                e.isAllowWhatsapp() ? e.getContactWhatsapp() : null,
                e.isAllowEmail() ? e.getContactEmail() : null,
                e.isAllowPhone(), e.isAllowWhatsapp(), e.isAllowEmail()
        );
        long helperCount = emergencyHelpRepository.countByEmergency(e);
        return new EmergencyDtos.EmergencyResponse(
                e.getId(), e.getCreatorUserId(), e.getType(),
                e.getTitle(), e.getDescription(),
                e.getArea(), e.getCity(), e.getState(), e.getCountry(), e.getLandmark(),
                e.getLocationDescription(), e.getLatitude(), e.getLongitude(),
                e.getStatus(), e.getEmergencyAt(), e.getCreatedAt(), e.getUpdatedAt(),
                helperCount, e.getViewCount(), e.getContactClickCount(),
                e.isResolvedByExternal(), e.getExternalHelperNote(),
                contact
        );
    }

    private void logEvent(Long emergencyId, UUID actorUserId,
                          EmergencyAuditLog.EventType type, String detailsJson) {
        EmergencyAuditLog log = new EmergencyAuditLog();
        log.setEmergencyId(emergencyId);
        log.setActorUserId(actorUserId);
        log.setEventType(type);
        log.setDetailsJson(detailsJson);
        auditLogRepository.save(log);
    }

    private String escape(String value) {
        if (value == null) return null;
        return value.replace("\"", "\\\"");
    }
}
