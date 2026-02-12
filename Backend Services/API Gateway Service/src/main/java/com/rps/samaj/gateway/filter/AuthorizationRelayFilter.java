package com.rps.samaj.gateway.filter;

import org.springframework.web.servlet.function.ServerRequest;

import java.util.function.Function;

/**
 * Copies Authorization header and adds X-Forwarded-* headers for backends behind the gateway.
 * Ensures OAuth redirects and other URLs use the gateway host (9551), not the backend host.
 */
public final class AuthorizationRelayFilter {

    private AuthorizationRelayFilter() {}

    public static Function<ServerRequest, ServerRequest> relay() {
        return request -> {
            ServerRequest.Builder builder = ServerRequest.from(request);

            String auth = request.headers().firstHeader("Authorization");
            if (auth != null && !auth.isBlank()) {
                builder.header("Authorization", auth);
            }

            String host = request.headers().firstHeader("Host");
            if (host != null && !host.isBlank()) {
                builder.header("X-Forwarded-Host", host);
            }
            builder.header("X-Forwarded-Proto", request.servletRequest().isSecure() ? "https" : "http");

            return builder.build();
        };
    }
}
