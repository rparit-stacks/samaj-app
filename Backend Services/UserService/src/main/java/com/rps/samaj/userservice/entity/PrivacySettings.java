package com.rps.samaj.userservice.entity;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "privacy_settings")
public class PrivacySettings {

    public enum ProfileVisibility {
        PUBLIC,
        MEMBERS_ONLY,
        PRIVATE
    }

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "show_email", nullable = false)
    private boolean showEmail = true;

    @Column(name = "show_blood_group", nullable = false)
    private boolean showBloodGroup = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_visibility", nullable = false)
    private ProfileVisibility profileVisibility = ProfileVisibility.MEMBERS_ONLY;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public boolean isShowEmail() {
        return showEmail;
    }

    public void setShowEmail(boolean showEmail) {
        this.showEmail = showEmail;
    }

    public boolean isShowBloodGroup() {
        return showBloodGroup;
    }

    public void setShowBloodGroup(boolean showBloodGroup) {
        this.showBloodGroup = showBloodGroup;
    }

    public ProfileVisibility getProfileVisibility() {
        return profileVisibility;
    }

    public void setProfileVisibility(ProfileVisibility profileVisibility) {
        this.profileVisibility = profileVisibility;
    }
}

