package com.rps.samaj.auth.security;

import com.rps.samaj.auth.dto.AuthResponse;
import com.rps.samaj.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    public OAuth2SuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String googleId = oauth2User.getAttribute("sub");
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");

        AuthResponse authResponse = authService.findOrCreateGoogleUser(googleId, email, name);

        String redirectUrl = frontendUrl + "/auth/callback#"
                + "accessToken=" + URLEncoder.encode(authResponse.accessToken(), StandardCharsets.UTF_8)
                + "&refreshToken=" + URLEncoder.encode(authResponse.refreshToken(), StandardCharsets.UTF_8)
                + "&expiresIn=" + authResponse.expiresIn();

        response.sendRedirect(redirectUrl);
    }
}
