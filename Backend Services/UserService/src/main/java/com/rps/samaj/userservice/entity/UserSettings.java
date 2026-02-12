package com.rps.samaj.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "show_phone", nullable = false)
    private boolean showPhone = true;

    @Column(name = "show_in_directory", nullable = false)
    private boolean showInDirectory = true;

    @Column(name = "emergency_alerts", nullable = false)
    private boolean emergencyAlerts = true;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public boolean isShowPhone() {
        return showPhone;
    }

    public void setShowPhone(boolean showPhone) {
        this.showPhone = showPhone;
    }

    public boolean isShowInDirectory() {
        return showInDirectory;
    }

    public void setShowInDirectory(boolean showInDirectory) {
        this.showInDirectory = showInDirectory;
    }

    public boolean isEmergencyAlerts() {
        return emergencyAlerts;
    }

    public void setEmergencyAlerts(boolean emergencyAlerts) {
        this.emergencyAlerts = emergencyAlerts;
    }
}

