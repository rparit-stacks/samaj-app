package com.rps.samaj.auth.service;

import com.rps.samaj.auth.entity.OtpCode;
import com.rps.samaj.auth.repository.OtpCodeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class OtpService {

    private static final Logger log = LoggerFactory.getLogger(OtpService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final OtpCodeRepository otpCodeRepository;

    @Value("${app.otp.enabled:true}")
    private boolean otpEnabled;

    public OtpService(OtpCodeRepository otpCodeRepository) {
        this.otpCodeRepository = otpCodeRepository;
    }

    @Transactional
    public String generateAndSendOtp(String identifier, OtpCode.IdentifierType type,
                                     OtpCode.OtpPurpose purpose) {
        String code = generateOtp();

        // Invalidate previous OTPs for same identifier/purpose
        otpCodeRepository.invalidateExistingOtps(identifier, purpose);

        OtpCode otp = new OtpCode();
        otp.setIdentifier(identifier);
        otp.setIdentifierType(type);
        otp.setCode(code);
        otp.setPurpose(purpose);
        otp.setExpiresAt(Instant.now().plusSeconds(OTP_EXPIRY_MINUTES * 60L));

        otpCodeRepository.save(otp);

        // Always print OTP to console for dev/testing
        String otpLog = String.format("====== OTP ====== For: %s | Type: %s | Purpose: %s | Code: %s ======",
                identifier, type, purpose, code);
        System.out.println(otpLog);
        log.info(otpLog);

        // TODO: Integrate with actual SMS (Twilio) / Email (SendGrid) service
        if (otpEnabled) {
            sendOtp(identifier, type, code, purpose);
        }

        return code;
    }

    private String generateOtp() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private void sendOtp(String identifier, OtpCode.IdentifierType type, String code,
                         OtpCode.OtpPurpose purpose) {
        // Placeholder - integrate Twilio/SendGrid. OTP already printed to console above.
        if (type == OtpCode.IdentifierType.EMAIL) {
            log.info("Would send OTP email to {} (purpose: {}) - check console for code", identifier, purpose);
        } else {
            log.info("Would send OTP SMS to {} (purpose: {}) - check console for code", identifier, purpose);
        }
    }

    @Transactional
    public Optional<OtpCode> verifyOtp(String identifier, String code, OtpCode.OtpPurpose purpose) {
        Optional<OtpCode> opt = otpCodeRepository.findTopByIdentifierAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                identifier, purpose);

        if (opt.isEmpty()) return Optional.empty();

        OtpCode otp = opt.get();
        if (otp.isExpired()) return Optional.empty();
        if (otp.getAttempts() >= MAX_ATTEMPTS) return Optional.empty();
        otp.incrementAttempts();

        if (!otp.getCode().equals(code)) {
            otpCodeRepository.save(otp);
            return Optional.empty();
        }

        otp.setVerified(true);
        otpCodeRepository.save(otp);
        return Optional.of(otp);
    }
}
