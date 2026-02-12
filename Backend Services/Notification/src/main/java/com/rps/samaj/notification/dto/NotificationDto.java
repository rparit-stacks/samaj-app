package com.rps.samaj.notification.dto;

import com.rps.samaj.notification.entity.Notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        String title,
        String body,
        String type,
        boolean read,
        String link,
        Instant createdAt
) {

    public static NotificationDto from(Notification n) {
        return new NotificationDto(
                n.getId(),
                n.getTitle(),
                n.getBody(),
                n.getType().name(),
                n.isRead(),
                n.getLink(),
                n.getCreatedAt()
        );
    }
}

