package com.rps.samaj.auth.service;

import com.rps.samaj.auth.dto.*;
import com.rps.samaj.auth.entity.OtpCode;
import com.rps.samaj.auth.entity.Session;
import com.rps.samaj.auth.entity.User;
import com.rps.samaj.auth.repository.SessionRepository;
import com.rps.samaj.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[0-9]{10,15}$");

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, OtpService otpService) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.otpService = otpService;
    }

    @Transactional
    public void register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (req.phone() != null && !req.phone().isBlank() && userRepository.existsByPhone(req.phone())) {
            throw new IllegalArgumentException("Phone already registered");
        }

        User user = new User();
        user.setEmail(req.email().trim().toLowerCase());
        user.setPhone(req.phone() != null && !req.phone().isBlank() ? req.phone().trim() : null);
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setStatus(User.UserStatus.PENDING_VERIFICATION);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        user = userRepository.save(user);
        // Send OTP for registration to email (primary identifier)
        otpService.generateAndSendOtp(user.getEmail(), OtpCode.IdentifierType.EMAIL, OtpCode.OtpPurpose.REGISTRATION);
    }

    @Transactional
    public AuthResponse login(LoginRequest req) {
        User user = findUserByIdentifier(req.identifier())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (user.getStatus() == User.UserStatus.SUSPENDED || user.getStatus() == User.UserStatus.DELETED) {
            throw new IllegalArgumentException("Account is suspended or deleted");
        }
        if (user.getPasswordHash() == null) {
            throw new IllegalArgumentException("Please login with Google");
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return createSessionAndRespond(user, null, null);
    }

    @Transactional
    public AuthResponse loginWithOtp(String identifier, String otp) {
        OtpCode.IdentifierType type = isEmail(identifier) ? OtpCode.IdentifierType.EMAIL : OtpCode.IdentifierType.PHONE;
        OtpCode.OtpPurpose purpose = OtpCode.OtpPurpose.LOGIN;

        OtpCode verified = otpService.verifyOtp(identifier, otp, purpose)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new IllegalArgumentException("User not found. Please register first."));

        if (user.getStatus() == User.UserStatus.SUSPENDED || user.getStatus() == User.UserStatus.DELETED) {
            throw new IllegalArgumentException("Account is suspended or deleted");
        }

        return createSessionAndRespond(user, null, null);
    }

    @Transactional
    public String sendOtp(OtpSendRequest req) {
        String identifier = req.identifier().trim();
        OtpCode.IdentifierType type = "PHONE".equalsIgnoreCase(req.type()) ? OtpCode.IdentifierType.PHONE : OtpCode.IdentifierType.EMAIL;
        OtpCode.OtpPurpose purpose = mapPurpose(req.purpose());

        String code = otpService.generateAndSendOtp(identifier, type, purpose);
        return "OTP sent successfully";
    }

    @Transactional
    public AuthResponse verifyOtpAndComplete(OtpVerifyRequest req) {
        String raw = req.identifier().trim();
        String identifier = isEmail(raw) ? raw.toLowerCase() : raw;
        OtpCode.OtpPurpose purpose = req.purpose() != null ? mapPurpose(req.purpose()) : OtpCode.OtpPurpose.REGISTRATION;

        otpService.verifyOtp(identifier, req.code() != null ? req.code().trim() : "", purpose)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        User user = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Registration via email OTP should also verify email
        if (purpose == OtpCode.OtpPurpose.REGISTRATION && isEmail(identifier)) {
            user.setEmailVerified(true);
        }
        if (purpose == OtpCode.OtpPurpose.EMAIL_VERIFICATION) user.setEmailVerified(true);
        if (purpose == OtpCode.OtpPurpose.PHONE_VERIFICATION) user.setPhoneVerified(true);
        if (user.getStatus() == User.UserStatus.PENDING_VERIFICATION) user.setStatus(User.UserStatus.ACTIVE);
        userRepository.save(user);

        return createSessionAndRespond(user, null, null);
    }

    @Transactional
    public void requestPasswordReset(String identifier) {
        User user = findUserByIdentifier(identifier).orElse(null);
        if (user != null && user.getPasswordHash() != null) {
            OtpCode.IdentifierType type = isEmail(identifier) ? OtpCode.IdentifierType.EMAIL : OtpCode.IdentifierType.PHONE;
            otpService.generateAndSendOtp(identifier, type, OtpCode.OtpPurpose.PASSWORD_RESET);
        }
    }

    @Transactional
    public AuthResponse resetPassword(PasswordResetRequest req) {
        OtpCode.IdentifierType type = isEmail(req.identifier()) ? OtpCode.IdentifierType.EMAIL : OtpCode.IdentifierType.PHONE;
        otpService.verifyOtp(req.identifier(), req.otp(), OtpCode.OtpPurpose.PASSWORD_RESET)
                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired OTP"));

        User user = findUserByIdentifier(req.identifier())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);

        return createSessionAndRespond(user, null, null);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest req) {
        Session session = sessionRepository.findByRefreshTokenAndRevokedFalse(req.refreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        if (session.isRefreshExpired()) {
            session.setRevoked(true);
            sessionRepository.save(session);
            throw new IllegalArgumentException("Refresh token expired");
        }
        User user = userRepository.findById(session.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getStatus() == User.UserStatus.SUSPENDED || user.getStatus() == User.UserStatus.DELETED) {
            throw new IllegalArgumentException("Account suspended or deleted");
        }
        sessionRepository.revokeByRefreshToken(req.refreshToken());
        return createSessionAndRespond(user, session.getUserAgent(), session.getIpAddress());
    }

    @Transactional
    public void logout(String refreshToken) {
        sessionRepository.findByRefreshTokenAndRevokedFalse(refreshToken).ifPresent(s -> {
            sessionRepository.revokeByRefreshToken(refreshToken);
        });
    }

    public Optional<UserResponse> getCurrentUser(UUID userId) {
        return userRepository.findById(userId).map(UserResponse::from);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getPasswordHash() == null) {
            throw new IllegalArgumentException("Please login with Google");
        }
        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setStatus(User.UserStatus.DELETED);
        user.setEmail(null);
        user.setPhone(null);
        user.setGoogleId(null);
        user.setPasswordHash(null);
        userRepository.save(user);
        sessionRepository.revokeAllByUserId(userId);
    }

    @Transactional
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (req.phone() != null && !req.phone().isBlank()) user.setPhone(req.phone().trim());
        if (req.name() != null) user.getMetadata().put("name", req.name());
        if (req.metadata() != null && !req.metadata().isEmpty()) user.getMetadata().putAll(req.metadata());
        user = userRepository.save(user);
        return UserResponse.from(user);
    }

    @Transactional
    public AuthResponse findOrCreateGoogleUser(String googleId, String email, String name) {
        Optional<User> existing = userRepository.findByGoogleId(googleId);
        if (existing.isPresent()) {
            User u = existing.get();
            if (u.getStatus() == User.UserStatus.SUSPENDED || u.getStatus() == User.UserStatus.DELETED) {
                throw new IllegalArgumentException("Account suspended or deleted");
            }
            return createSessionAndRespond(u, null, null);
        }

        User user = new User();
        user.setGoogleId(googleId);
        user.setEmail(email != null ? email.trim().toLowerCase() : null);
        user.setEmailVerified(true);
        user.setStatus(User.UserStatus.ACTIVE);
        user.setRole(User.UserRole.USER);
        user.getMetadata().put("name", name != null ? name : "");
        user = userRepository.save(user);
        return createSessionAndRespond(user, null, null);
    }

    private AuthResponse createSessionAndRespond(User user, String userAgent, String ipAddress) {
        String access = jwtService.generateAccessToken(user);
        String refresh = jwtService.generateRefreshToken(user);

        Session session = new Session();
        session.setUserId(user.getId());
        session.setAccessToken(access);
        session.setRefreshToken(refresh);
        session.setExpiresAt(jwtService.getAccessExpiryInstant());
        session.setRefreshExpiresAt(jwtService.getRefreshExpiryInstant());
        session.setUserAgent(userAgent);
        session.setIpAddress(ipAddress);
        sessionRepository.save(session);

        return AuthResponse.of(access, refresh, jwtService.getAccessExpirySeconds(), user);
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        identifier = identifier.trim();
        if (isEmail(identifier)) return userRepository.findByEmail(identifier);
        return userRepository.findByPhone(identifier);
    }

    private boolean isEmail(String s) {
        return EMAIL_PATTERN.matcher(s).matches();
    }

    private OtpCode.OtpPurpose mapPurpose(String p) {
        if (p == null) return OtpCode.OtpPurpose.REGISTRATION;
        return switch (p.toUpperCase()) {
            case "LOGIN" -> OtpCode.OtpPurpose.LOGIN;
            case "PASSWORD_RESET" -> OtpCode.OtpPurpose.PASSWORD_RESET;
            case "PHONE_VERIFICATION" -> OtpCode.OtpPurpose.PHONE_VERIFICATION;
            case "EMAIL_VERIFICATION" -> OtpCode.OtpPurpose.EMAIL_VERIFICATION;
            default -> OtpCode.OtpPurpose.REGISTRATION;
        };
    }
}
