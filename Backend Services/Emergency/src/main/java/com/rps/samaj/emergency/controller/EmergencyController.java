package com.rps.samaj.emergency.controller;

import com.rps.samaj.emergency.dto.EmergencyDtos;
import com.rps.samaj.emergency.service.EmergencyService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/emergencies")
public class EmergencyController {

    private final EmergencyService emergencyService;

    public EmergencyController(EmergencyService emergencyService) {
        this.emergencyService = emergencyService;
    }

    private UUID currentUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null)
            throw new IllegalStateException("Unauthenticated");
        return (UUID) authentication.getPrincipal();
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        for (GrantedAuthority a : authentication.getAuthorities())
            if ("ROLE_ADMIN".equals(a.getAuthority())) return true;
        return false;
    }

    /* ── CRUD ──────────────────────────────────────────────── */

    @PostMapping
    public ResponseEntity<EmergencyDtos.EmergencyResponse> create(
            @RequestBody EmergencyDtos.CreateEmergencyRequest request,
            Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(emergencyService.createEmergency(userId, request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmergencyDtos.EmergencyResponse> edit(
            @PathVariable Long id,
            @RequestBody EmergencyDtos.UpdateEmergencyRequest request,
            Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(emergencyService.editEmergency(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        UUID userId = currentUserId(authentication);
        emergencyService.deleteEmergency(id, userId);
        return ResponseEntity.noContent().build();
    }

    /* ── Resolve (with helper selection) ──────────────────── */

    @PostMapping("/{id}/resolve")
    public ResponseEntity<EmergencyDtos.EmergencyResponse> resolve(
            @PathVariable Long id,
            @RequestBody EmergencyDtos.ResolveEmergencyRequest request,
            Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(emergencyService.resolveEmergency(id, userId, request));
    }

    /* ── Views & Contact tracking ─────────────────────────── */

    @PostMapping("/{id}/view")
    public ResponseEntity<Void> trackView(@PathVariable Long id) {
        emergencyService.trackView(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/contact-click")
    public ResponseEntity<Void> trackContactClick(@PathVariable Long id) {
        emergencyService.trackContactClick(id);
        return ResponseEntity.ok().build();
    }

    /* ── Listings ─────────────────────────────────────────── */

    @GetMapping
    public ResponseEntity<List<EmergencyDtos.EmergencyResponse>> listAll() {
        return ResponseEntity.ok(emergencyService.listAll());
    }

    @GetMapping("/me")
    public ResponseEntity<List<EmergencyDtos.EmergencyResponse>> myEmergencies(Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(emergencyService.listMyEmergencies(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmergencyDtos.EmergencyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(emergencyService.getById(id));
    }

    /* ── Dashboard stats ──────────────────────────────────── */

    @GetMapping("/dashboard")
    public ResponseEntity<EmergencyDtos.DashboardStatsResponse> dashboardStats(Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(emergencyService.dashboardStats(userId));
    }

    /* ── Status update (generic) ──────────────────────────── */

    @PatchMapping("/{id}/status")
    public ResponseEntity<EmergencyDtos.EmergencyResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody EmergencyDtos.UpdateEmergencyStatusRequest request,
            Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(emergencyService.updateStatus(id, userId, request));
    }

    /* ── Helpers ──────────────────────────────────────────── */

    @PostMapping("/{id}/helpers")
    public ResponseEntity<EmergencyDtos.EmergencyHelpResponse> markHelper(
            @PathVariable Long id,
            @RequestBody EmergencyDtos.MarkHelperRequest request,
            Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(emergencyService.markHelper(id, userId, request));
    }

    @GetMapping("/{id}/helpers")
    public ResponseEntity<List<EmergencyDtos.EmergencyHelpResponse>> helpersForEmergency(
            @PathVariable Long id, Authentication authentication) {
        UUID userId = currentUserId(authentication);
        return ResponseEntity.ok(emergencyService.helpersForEmergency(id, userId));
    }

    @GetMapping("/helpers/{helperUserId}/stats")
    public ResponseEntity<EmergencyDtos.HelperStatsResponse> helperStats(
            @PathVariable UUID helperUserId) {
        return ResponseEntity.ok(emergencyService.helperStats(helperUserId));
    }

    /* ── Admin-only audit ─────────────────────────────────── */

    @GetMapping("/{id}/audit")
    public ResponseEntity<?> getAuditForEmergency(
            @PathVariable Long id, Authentication authentication) {
        if (!isAdmin(authentication)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(emergencyService.getAuditForEmergency(id));
    }

    @GetMapping("/audit/search")
    public ResponseEntity<?> searchAuditByActor(
            @RequestParam UUID actorUserId, Authentication authentication) {
        if (!isAdmin(authentication)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(emergencyService.getAuditForActor(actorUserId));
    }
}
