package com.rps.samaj.userservice.service;

import com.rps.samaj.userservice.dto.*;
import com.rps.samaj.userservice.entity.*;
import com.rps.samaj.userservice.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final PrivacySettingsRepository privacySettingsRepository;
    private final SecuritySettingsRepository securitySettingsRepository;

    public UserProfileService(UserProfileRepository userProfileRepository,
                              FamilyMemberRepository familyMemberRepository,
                              UserSettingsRepository userSettingsRepository,
                              PrivacySettingsRepository privacySettingsRepository,
                              SecuritySettingsRepository securitySettingsRepository) {
        this.userProfileRepository = userProfileRepository;
        this.familyMemberRepository = familyMemberRepository;
        this.userSettingsRepository = userSettingsRepository;
        this.privacySettingsRepository = privacySettingsRepository;
        this.securitySettingsRepository = securitySettingsRepository;
    }

    /* Profile */

    @Transactional
    public UserProfile getOrCreateProfile(UUID userId) {
        return userProfileRepository.findById(userId).orElseGet(() -> {
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            return userProfileRepository.save(profile);
        });
    }

    @Transactional
    public UserProfile updateProfile(UUID userId, UserProfileDto dto) {
        UserProfile profile = getOrCreateProfile(userId);
        if (dto.fullName() != null) profile.setFullName(dto.fullName());
        if (dto.city() != null) profile.setCity(dto.city());
        if (dto.profession() != null) profile.setProfession(dto.profession());
        if (dto.bio() != null) profile.setBio(dto.bio());
        if (dto.avatarUrl() != null) profile.setAvatarUrl(dto.avatarUrl());
        if (dto.coverImageUrl() != null) profile.setCoverImageUrl(dto.coverImageUrl().isBlank() ? null : dto.coverImageUrl());
        return userProfileRepository.save(profile);
    }

    /* Family */

    public List<FamilyMember> getFamily(UUID userId) {
        return familyMemberRepository.findByUserId(userId);
    }

    @Transactional
    public FamilyMember addFamilyMember(UUID userId, FamilyMemberDto dto) {
        FamilyMember fm = new FamilyMember();
        fm.setUserId(userId);
        fm.setName(dto.name());
        fm.setRelation(dto.relation());
        fm.setCity(dto.city());
        fm.setPhone(dto.phone());
        fm.setEmail(dto.email());
        return familyMemberRepository.save(fm);
    }

    @Transactional
    public FamilyMember updateFamilyMember(UUID userId, UUID memberId, FamilyMemberDto dto) {
        FamilyMember fm = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Family member not found"));
        if (!fm.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not allowed");
        }
        fm.setName(dto.name());
        fm.setRelation(dto.relation());
        fm.setCity(dto.city());
        fm.setPhone(dto.phone());
        fm.setEmail(dto.email());
        return familyMemberRepository.save(fm);
    }

    @Transactional
    public void deleteFamilyMember(UUID userId, UUID memberId) {
        FamilyMember fm = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Family member not found"));
        if (!fm.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not allowed");
        }
        familyMemberRepository.delete(fm);
    }

    /* Settings */

    @Transactional
    public UserSettings getOrCreateSettings(UUID userId) {
        return userSettingsRepository.findById(userId).orElseGet(() -> {
            UserSettings s = new UserSettings();
            s.setUserId(userId);
            return userSettingsRepository.save(s);
        });
    }

    @Transactional
    public UserSettings updateSettings(UUID userId, UserSettingsDto dto) {
        UserSettings s = getOrCreateSettings(userId);
        s.setShowPhone(dto.showPhone());
        s.setShowInDirectory(dto.showInDirectory());
        s.setEmergencyAlerts(dto.emergencyAlerts());
        return userSettingsRepository.save(s);
    }

    /* Privacy */

    @Transactional
    public PrivacySettings getOrCreatePrivacy(UUID userId) {
        return privacySettingsRepository.findById(userId).orElseGet(() -> {
            PrivacySettings p = new PrivacySettings();
            p.setUserId(userId);
            return privacySettingsRepository.save(p);
        });
    }

    @Transactional
    public PrivacySettings updatePrivacy(UUID userId, PrivacySettingsDto dto) {
        PrivacySettings p = getOrCreatePrivacy(userId);
        p.setShowEmail(dto.showEmail());
        p.setShowBloodGroup(dto.showBloodGroup());
        if (dto.profileVisibility() != null) {
            p.setProfileVisibility(PrivacySettings.ProfileVisibility.valueOf(dto.profileVisibility()));
        }
        return privacySettingsRepository.save(p);
    }

    /* Security */

    @Transactional
    public SecuritySettings getOrCreateSecurity(UUID userId) {
        return securitySettingsRepository.findById(userId).orElseGet(() -> {
            SecuritySettings s = new SecuritySettings();
            s.setUserId(userId);
            return securitySettingsRepository.save(s);
        });
    }

    @Transactional
    public SecuritySettings updateSecurity(UUID userId, SecuritySettingsDto dto) {
        SecuritySettings s = getOrCreateSecurity(userId);
        s.setTwoFactorEnabled(dto.twoFactorEnabled());
        s.setLoginAlertsEnabled(dto.loginAlertsEnabled());
        return securitySettingsRepository.save(s);
    }
}

