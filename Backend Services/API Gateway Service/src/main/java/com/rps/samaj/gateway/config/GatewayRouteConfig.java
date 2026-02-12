package com.rps.samaj.gateway.config;

import com.rps.samaj.gateway.filter.AuthorizationRelayFilter;
import org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouterFunction<ServerResponse> authRoutes() {
        return GatewayRouterFunctions.route("auth_service")
                .GET("/auth/**", HandlerFunctions.http())
                .POST("/auth/**", HandlerFunctions.http())
                .PUT("/auth/**", HandlerFunctions.http())
                .DELETE("/auth/**", HandlerFunctions.http())
                .OPTIONS("/auth/**", HandlerFunctions.http())
                .GET("/oauth2/**", HandlerFunctions.http())
                .POST("/oauth2/**", HandlerFunctions.http())
                .OPTIONS("/oauth2/**", HandlerFunctions.http())
                .GET("/login/oauth2/**", HandlerFunctions.http())
                .POST("/login/oauth2/**", HandlerFunctions.http())
                .OPTIONS("/login/oauth2/**", HandlerFunctions.http())
                .before(AuthorizationRelayFilter.relay())
                .before(BeforeFilterFunctions.uri("http://localhost:8081"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoutes() {
        return GatewayRouterFunctions.route("user_service")
                .GET("/api/v1/users/**", HandlerFunctions.http())
                .POST("/api/v1/users/**", HandlerFunctions.http())
                .PUT("/api/v1/users/**", HandlerFunctions.http())
                .DELETE("/api/v1/users/**", HandlerFunctions.http())
                .OPTIONS("/api/v1/users/**", HandlerFunctions.http())
                .before(AuthorizationRelayFilter.relay())
                .before(BeforeFilterFunctions.uri("http://localhost:8082"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoutes() {
        return GatewayRouterFunctions.route("notification_service")

                // PUBLIC APIs
                .GET("/api/v1/notifications/**", HandlerFunctions.http())
                .before(BeforeFilterFunctions.uri("http://localhost:8083"))

                // SECURED APIs
                .POST("/api/v1/notifications/**", HandlerFunctions.http())
                .PUT("/api/v1/notifications/**", HandlerFunctions.http())
                .DELETE("/api/v1/notifications/**", HandlerFunctions.http())
                .OPTIONS("/api/v1/notifications/**", HandlerFunctions.http())
                .before(AuthorizationRelayFilter.relay())
                .before(BeforeFilterFunctions.uri("http://localhost:8083"))

                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> cloudServiceRoutes() {
        return GatewayRouterFunctions.route("cloud_service")
                .GET("/api/cloud/**", HandlerFunctions.http())
                .POST("/api/cloud/**", HandlerFunctions.http())
                .PUT("/api/cloud/**", HandlerFunctions.http())
                .DELETE("/api/cloud/**", HandlerFunctions.http())
                .OPTIONS("/api/cloud/**", HandlerFunctions.http())
                .before(AuthorizationRelayFilter.relay())
                .before(BeforeFilterFunctions.uri("http://localhost:8084"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> newsServiceRoutes() {
        return GatewayRouterFunctions.route("news_service")
                .GET("/api/v1/news/**", HandlerFunctions.http())
                .POST("/api/v1/news/**", HandlerFunctions.http())
                .PUT("/api/v1/news/**", HandlerFunctions.http())
                .DELETE("/api/v1/news/**", HandlerFunctions.http())
                .OPTIONS("/api/v1/news/**", HandlerFunctions.http())
                .before(AuthorizationRelayFilter.relay())
                // For now we route directly by host:port; later can switch to lb:// with Eureka
                .before(BeforeFilterFunctions.uri("http://localhost:8085"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> emergencyServiceRoutes() {
        return GatewayRouterFunctions.route("emergency_service")
                .GET("/api/v1/emergencies", HandlerFunctions.http())
                .GET("/api/v1/emergencies/**", HandlerFunctions.http())
                .POST("/api/v1/emergencies", HandlerFunctions.http())
                .POST("/api/v1/emergencies/**", HandlerFunctions.http())
                .PUT("/api/v1/emergencies/**", HandlerFunctions.http())
                .PATCH("/api/v1/emergencies/**", HandlerFunctions.http())
                .DELETE("/api/v1/emergencies/**", HandlerFunctions.http())
                .OPTIONS("/api/v1/emergencies", HandlerFunctions.http())
                .OPTIONS("/api/v1/emergencies/**", HandlerFunctions.http())
                .before(AuthorizationRelayFilter.relay())
                .before(BeforeFilterFunctions.uri("http://localhost:8086"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> communityServiceRoutes() {
        return GatewayRouterFunctions.route("community_service")
                .GET("/api/v1/community/**", HandlerFunctions.http())
                .POST("/api/v1/community/**", HandlerFunctions.http())
                .PUT("/api/v1/community/**", HandlerFunctions.http())
                .DELETE("/api/v1/community/**", HandlerFunctions.http())
                .OPTIONS("/api/v1/community/**", HandlerFunctions.http())
                .before(AuthorizationRelayFilter.relay())
                .before(BeforeFilterFunctions.uri("http://localhost:8087"))
                .build();
    }
}
