package com.rps.samaj.notification.controller;

import com.rps.samaj.notification.dto.NotificationDto;
import com.rps.samaj.notification.dto.NotificationPreferenceDto;
import com.rps.samaj.notification.entity.Notification;
import com.rps.samaj.notification.entity.NotificationPreference;
import com.rps.samaj.notification.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    private UUID getUserId(Object attr) {
        if (attr instanceof UUID uuid) return uuid;
        if (attr instanceof String s) return UUID.fromString(s);
        throw new IllegalArgumentException("Invalid user id");
    }

    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            @RequestAttribute("userId") Object userIdAttr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean unreadOnly
    ) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(notificationService.getNotifications(userId, page, size, unreadOnly));
    }

    @GetMapping("/unread")
    public ResponseEntity<Map<String, Long>> unreadCount(@RequestAttribute("userId") Object userIdAttr) {
        UUID userId = getUserId(userIdAttr);
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unread", count));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@RequestAttribute("userId") Object userIdAttr,
                                         @PathVariable UUID id) {
        UUID userId = getUserId(userIdAttr);
        notificationService.markRead(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead(@RequestAttribute("userId") Object userIdAttr) {
        UUID userId = getUserId(userIdAttr);
        notificationService.markAllRead(userId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@RequestAttribute("userId") Object userIdAttr,
                                       @PathVariable UUID id) {
        UUID userId = getUserId(userIdAttr);
        notificationService.delete(userId, id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clear(@RequestAttribute("userId") Object userIdAttr) {
        UUID userId = getUserId(userIdAttr);
        notificationService.clear(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferences")
    public ResponseEntity<NotificationPreference> getPreferences(@RequestAttribute("userId") Object userIdAttr) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(notificationService.getOrCreatePreferences(userId));
    }

    @PutMapping("/preferences")
    public ResponseEntity<NotificationPreference> updatePreferences(
            @RequestAttribute("userId") Object userIdAttr,
            @RequestBody NotificationPreferenceDto dto
    ) {
        UUID userId = getUserId(userIdAttr);
        return ResponseEntity.ok(notificationService.updatePreferences(userId, dto));
    }
}

