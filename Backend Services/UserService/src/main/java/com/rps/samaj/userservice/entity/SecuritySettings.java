package com.rps.samaj.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "security_settings")
public class SecuritySettings {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled = false;

    @Column(name = "login_alerts_enabled", nullable = false)
    private boolean loginAlertsEnabled = true;

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public boolean isTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    public void setTwoFactorEnabled(boolean twoFactorEnabled) {
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public boolean isLoginAlertsEnabled() {
        return loginAlertsEnabled;
    }

    public void setLoginAlertsEnabled(boolean loginAlertsEnabled) {
        this.loginAlertsEnabled = loginAlertsEnabled;
    }
}

