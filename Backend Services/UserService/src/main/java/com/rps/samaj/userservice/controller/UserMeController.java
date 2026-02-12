package com.rps.samaj.userservice.controller;

import com.rps.samaj.userservice.dto.*;
import com.rps.samaj.userservice.entity.*;
import com.rps.samaj.userservice.service.UserProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/me")
public class UserMeController {

    private final UserProfileService userProfileService;

    public UserMeController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    private UUID getUserId(Object attr) {
        if (attr instanceof UUID uuid) return uuid;
        if (attr instanceof String s) return UUID.fromString(s);
        throw new IllegalArgumentException("Invalid user id");
    }

    /* Profile */

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getProfile(@RequestAttribute("userId") Object userIdAttr) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.getOrCreateProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfile> updateProfile(@RequestAttribute("userId") Object userIdAttr,
                                                     @Valid @RequestBody UserProfileDto dto) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.updateProfile(userId, dto));
    }

    /* Family */

    @GetMapping("/family")
    public ResponseEntity<List<FamilyMember>> getFamily(@RequestAttribute("userId") Object userIdAttr) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.getFamily(userId));
    }

    @PostMapping("/family")
    public ResponseEntity<FamilyMember> addFamilyMember(@RequestAttribute("userId") Object userIdAttr,
                                                        @Valid @RequestBody FamilyMemberDto dto) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.addFamilyMember(userId, dto));
    }

    @PutMapping("/family/{memberId}")
    public ResponseEntity<FamilyMember> updateFamilyMember(@RequestAttribute("userId") Object userIdAttr,
                                                           @PathVariable UUID memberId,
                                                           @Valid @RequestBody FamilyMemberDto dto) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.updateFamilyMember(userId, memberId, dto));
    }

    @DeleteMapping("/family/{memberId}")
    public ResponseEntity<Void> deleteFamilyMember(@RequestAttribute("userId") Object userIdAttr,
                                                   @PathVariable UUID memberId) {
        UUID userId = getUserId(userIdAttr);
        userProfileService.deleteFamilyMember(userId, memberId);
        return ResponseEntity.noContent().build();
    }

    /* Settings */

    @GetMapping("/settings")
    public ResponseEntity<UserSettings> getSettings(@RequestAttribute("userId") Object userIdAttr) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.getOrCreateSettings(userId));
    }

    @PutMapping("/settings")
    public ResponseEntity<UserSettings> updateSettings(@RequestAttribute("userId") Object userIdAttr,
                                                       @RequestBody UserSettingsDto dto) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.updateSettings(userId, dto));
    }

    /* Privacy */

    @GetMapping("/privacy")
    public ResponseEntity<PrivacySettings> getPrivacy(@RequestAttribute("userId") Object userIdAttr) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.getOrCreatePrivacy(userId));
    }

    @PutMapping("/privacy")
    public ResponseEntity<PrivacySettings> updatePrivacy(@RequestAttribute("userId") Object userIdAttr,
                                                         @RequestBody PrivacySettingsDto dto) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.updatePrivacy(userId, dto));
    }

    /* Security */

    @GetMapping("/security")
    public ResponseEntity<SecuritySettings> getSecurity(@RequestAttribute("userId") Object userIdAttr) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.getOrCreateSecurity(userId));
    }

    @PutMapping("/security")
    public ResponseEntity<SecuritySettings> updateSecurity(@RequestAttribute("userId") Object userIdAttr,
                                                           @RequestBody SecuritySettingsDto dto) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(userProfileService.updateSecurity(userId, dto));
    }
}

