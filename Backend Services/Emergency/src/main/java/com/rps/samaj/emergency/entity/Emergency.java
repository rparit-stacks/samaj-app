package com.rps.samaj.emergency.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "emergencies")
public class Emergency {

    public enum Status {
        OPEN,
        IN_PROGRESS,
        HELP_RECEIVED,
        RESOLVED,
        CANCELLED,
        CLOSED
    }

    public enum Type {
        MEDICAL,
        ACCIDENT,
        FINANCIAL,
        BLOOD,
        OTHER
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_user_id", nullable = false, updatable = false)
    private UUID creatorUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 32)
    private Type type = Type.OTHER;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(name = "area", length = 255)
    private String area;

    @Column(name = "city", length = 255)
    private String city;

    @Column(name = "state", length = 255)
    private String state;

    @Column(name = "country", length = 255)
    private String country;

    @Column(name = "landmark", length = 255)
    private String landmark;

    @Column(name = "location_description", length = 1000)
    private String locationDescription;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "contact_whatsapp", length = 50)
    private String contactWhatsapp;

    @Column(name = "contact_email", length = 255)
    private String contactEmail;

    @Column(name = "allow_phone", nullable = false)
    private boolean allowPhone = false;

    @Column(name = "allow_whatsapp", nullable = false)
    private boolean allowWhatsapp = false;

    @Column(name = "allow_email", nullable = false)
    private boolean allowEmail = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.OPEN;

    @Column(name = "view_count", nullable = false)
    private long viewCount = 0;

    @Column(name = "contact_click_count", nullable = false)
    private long contactClickCount = 0;

    @Column(name = "resolved_by_external", nullable = false)
    private boolean resolvedByExternal = false;

    @Column(name = "external_helper_note", length = 1000)
    private String externalHelperNote;

    @Column(name = "emergency_at", nullable = false)
    private Instant emergencyAt = Instant.now();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    // Getters & setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public UUID getCreatorUserId() { return creatorUserId; }
    public void setCreatorUserId(UUID creatorUserId) { this.creatorUserId = creatorUserId; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }

    public String getLocationDescription() { return locationDescription; }
    public void setLocationDescription(String ld) { this.locationDescription = ld; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }

    public String getContactWhatsapp() { return contactWhatsapp; }
    public void setContactWhatsapp(String contactWhatsapp) { this.contactWhatsapp = contactWhatsapp; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public boolean isAllowPhone() { return allowPhone; }
    public void setAllowPhone(boolean allowPhone) { this.allowPhone = allowPhone; }

    public boolean isAllowWhatsapp() { return allowWhatsapp; }
    public void setAllowWhatsapp(boolean allowWhatsapp) { this.allowWhatsapp = allowWhatsapp; }

    public boolean isAllowEmail() { return allowEmail; }
    public void setAllowEmail(boolean allowEmail) { this.allowEmail = allowEmail; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public long getViewCount() { return viewCount; }
    public void setViewCount(long viewCount) { this.viewCount = viewCount; }
    public void incrementViewCount() { this.viewCount++; }

    public long getContactClickCount() { return contactClickCount; }
    public void setContactClickCount(long contactClickCount) { this.contactClickCount = contactClickCount; }
    public void incrementContactClickCount() { this.contactClickCount++; }

    public boolean isResolvedByExternal() { return resolvedByExternal; }
    public void setResolvedByExternal(boolean resolvedByExternal) { this.resolvedByExternal = resolvedByExternal; }

    public String getExternalHelperNote() { return externalHelperNote; }
    public void setExternalHelperNote(String externalHelperNote) { this.externalHelperNote = externalHelperNote; }

    public Instant getEmergencyAt() { return emergencyAt; }
    public void setEmergencyAt(Instant emergencyAt) { this.emergencyAt = emergencyAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
