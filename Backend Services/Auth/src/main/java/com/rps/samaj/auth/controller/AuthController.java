package com.rps.samaj.auth.controller;

import com.rps.samaj.auth.dto.*;
import com.rps.samaj.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Registration initiated. Please verify OTP sent to your email.",
                        "otpRequired", true
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/login/otp")
    public ResponseEntity<AuthResponse> loginWithOtp(@RequestBody Map<String, String> body) {
        String identifier = body.get("identifier");
        String otp = body.get("otp");
        if (identifier == null || otp == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.loginWithOtp(identifier.trim(), otp.trim()));
    }

    @PostMapping("/otp/send")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyOtpAndComplete(request));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<Map<String, String>> requestPasswordReset(@RequestBody Map<String, String> body) {
        String identifier = body.get("identifier");
        if (identifier == null || identifier.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        authService.requestPasswordReset(identifier.trim());
        return ResponseEntity.ok(Map.of("message", "If account exists, password reset OTP has been sent"));
    }

    @PostMapping("/password/reset/confirm")
    public ResponseEntity<AuthResponse> confirmPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> body,
                                                      HttpServletRequest req) {
        String refreshToken = body != null ? body.get("refreshToken") : null;
        if (refreshToken == null && req.getHeader("Authorization") != null) {
            String auth = req.getHeader("Authorization");
            if (auth.startsWith("Bearer ")) refreshToken = auth.substring(7);
        }
        if (refreshToken != null) authService.logout(refreshToken);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@RequestAttribute("userId") UUID userId) {
        return authService.getCurrentUser(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateProfile(@RequestAttribute("userId") UUID userId,
                                                      @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(authService.updateProfile(userId, request));
    }

    @PostMapping("/password/change")
    public ResponseEntity<Map<String, String>> changePassword(@RequestAttribute("userId") UUID userId,
                                                              @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }

    @DeleteMapping("/account")
    public ResponseEntity<Map<String, String>> deleteAccount(@RequestAttribute("userId") UUID userId) {
        authService.deleteAccount(userId);
        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
    }
}
