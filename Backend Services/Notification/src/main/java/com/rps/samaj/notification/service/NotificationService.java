package com.rps.samaj.notification.service;

import com.rps.samaj.notification.dto.NotificationDto;
import com.rps.samaj.notification.dto.NotificationPreferenceDto;
import com.rps.samaj.notification.entity.Notification;
import com.rps.samaj.notification.entity.NotificationPreference;
import com.rps.samaj.notification.repository.NotificationPreferenceRepository;
import com.rps.samaj.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;
    private final EmailNotificationSender emailSender;

    public NotificationService(NotificationRepository notificationRepository,
                               NotificationPreferenceRepository preferenceRepository,
                               EmailNotificationSender emailSender) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
        this.emailSender = emailSender;
    }

    public Page<NotificationDto> getNotifications(UUID userId, int page, int size, boolean unreadOnly) {
        PageRequest pr = PageRequest.of(page, size);
        Page<Notification> notifications = unreadOnly
                ? notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false, pr)
                : notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pr);
        return notifications.map(NotificationDto::from);
    }

    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }

    @Transactional
    public void markRead(UUID userId, UUID id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not allowed");
        }
        n.setRead(true);
        notificationRepository.save(n);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        List<Notification> list = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(userId, false, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
    }

    @Transactional
    public void delete(UUID userId, UUID id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        if (!n.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Not allowed");
        }
        notificationRepository.delete(n);
    }

    @Transactional
    public void clear(UUID userId) {
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, Integer.MAX_VALUE)).getContent();
        notificationRepository.deleteAll(list);
    }

    @Transactional
    public NotificationPreference getOrCreatePreferences(UUID userId) {
        return preferenceRepository.findById(userId).orElseGet(() -> {
            NotificationPreference p = new NotificationPreference();
            p.setUserId(userId);
            return preferenceRepository.save(p);
        });
    }

    @Transactional
    public NotificationPreference updatePreferences(UUID userId, NotificationPreferenceDto dto) {
        NotificationPreference p = getOrCreatePreferences(userId);
        p.setEmailEnabled(dto.emailEnabled());
        p.setInAppEnabled(dto.inAppEnabled());
        p.setSecurityEmailEnabled(dto.securityEmailEnabled());
        return preferenceRepository.save(p);
    }

    @Transactional
    public Notification sendNotification(UUID userId, String userEmail, String title, String body, Notification.Type type, String link) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setBody(body);
        n.setType(type);
        n.setLink(link);
        n = notificationRepository.save(n);
        emailSender.sendIfEnabled(userId, userEmail, n);
        return n;
    }
}

