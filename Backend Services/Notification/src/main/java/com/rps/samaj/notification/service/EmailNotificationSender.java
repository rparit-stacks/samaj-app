package com.rps.samaj.notification.service;

import com.rps.samaj.notification.entity.Notification;
import com.rps.samaj.notification.entity.NotificationPreference;
import com.rps.samaj.notification.repository.NotificationPreferenceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class EmailNotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final JavaMailSender mailSender;
    private final NotificationPreferenceRepository preferenceRepository;

    public EmailNotificationSender(JavaMailSender mailSender, NotificationPreferenceRepository preferenceRepository) {
        this.mailSender = mailSender;
        this.preferenceRepository = preferenceRepository;
    }

    public void sendIfEnabled(UUID userId, String userEmail, Notification notification) {
        Optional<NotificationPreference> prefOpt = preferenceRepository.findById(userId);
        NotificationPreference pref = prefOpt.orElseGet(() -> {
            NotificationPreference p = new NotificationPreference();
            p.setUserId(userId);
            return preferenceRepository.save(p);
        });

        boolean shouldSend = pref.isEmailEnabled();
        if (notification.getType() == Notification.Type.SECURITY) {
            shouldSend = shouldSend && pref.isSecurityEmailEnabled();
        }

        if (!shouldSend || userEmail == null || userEmail.isBlank()) {
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(userEmail);
            msg.setSubject(notification.getTitle());
            msg.setText(notification.getBody());
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Failed to send notification email: {}", e.getMessage());
        }
    }
}

