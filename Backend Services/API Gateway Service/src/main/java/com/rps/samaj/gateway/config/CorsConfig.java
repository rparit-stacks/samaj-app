package com.rps.samaj.gateway.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Gateway-level CORS filter. This is the ONLY place CORS headers are managed.
 * Backend services must have CORS disabled (.cors(cors -> cors.disable())).
 *
 * Uses a response wrapper that blocks any Access-Control-* headers from
 * downstream (proxy → backend) to guarantee no duplicates.
 */
@Configuration
public class CorsConfig {

    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
            "http://localhost:8080",
            "http://127.0.0.1:8080",
            "http://192.168.1.6:8080",
            "http://192.168.137.1:8080"
    );

    @Bean
    public FilterRegistrationBean<Filter> corsFilter() {
        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new GatewayCorsFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

    private static class GatewayCorsFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {

            String origin = request.getHeader("Origin");

            if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
                response.setHeader("Access-Control-Allow-Origin", origin);
                response.setHeader("Access-Control-Allow-Credentials", "true");
                response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS,PATCH");
                response.setHeader("Access-Control-Allow-Headers", "Authorization,Content-Type,X-Auth-Token,Accept,Origin");
                response.setHeader("Access-Control-Expose-Headers", "Authorization,Content-Type");
                response.setHeader("Access-Control-Max-Age", "3600");
            }

            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                response.setStatus(HttpServletResponse.SC_OK);
                return;
            }

            // Wrap response to block any CORS headers from the proxied backend
            filterChain.doFilter(request, new CorsHeaderBlockingResponse(response));
        }
    }

    /**
     * Wrapper that silently drops any Access-Control-* headers set by downstream
     * (e.g. the proxied backend service). The gateway filter already set the
     * correct CORS headers on the REAL response before this wrapper was created.
     */
    private static class CorsHeaderBlockingResponse extends HttpServletResponseWrapper {

        public CorsHeaderBlockingResponse(HttpServletResponse response) {
            super(response);
        }

        @Override
        public void setHeader(String name, String value) {
            if (name != null && name.toLowerCase().startsWith("access-control-")) {
                return; // block
            }
            super.setHeader(name, value);
        }

        @Override
        public void addHeader(String name, String value) {
            if (name != null && name.toLowerCase().startsWith("access-control-")) {
                return; // block
            }
            super.addHeader(name, value);
        }
    }
}
