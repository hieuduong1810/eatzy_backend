package com.example.FoodDelivery.config;

import java.util.Map;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.example.FoodDelivery.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor to authenticate WebSocket connections using JWT token.
 * Token is passed via query parameter: /ws?token=xxx
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final SecurityUtil securityUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;

            // Get token from query parameter
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null && !token.isEmpty()) {
                try {
                    // Validate token and extract email (subject)
                    Jwt jwt = securityUtil.checkValidRefreshToken(token);
                    String email = jwt.getSubject();

                    if (email != null && !email.isEmpty()) {
                        // Store email in session attributes for later use
                        attributes.put("email", email);
                        attributes.put("userId", jwt.getClaim("user"));
                        log.info("WebSocket authenticated for user: {}", email);
                        return true; // Allow connection
                    }
                } catch (Exception e) {
                    log.error("WebSocket authentication failed: {}", e.getMessage());
                }
            }
        }

        log.warn("WebSocket connection rejected - no valid token provided");
        return false; // Reject connection
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception exception) {
        // No action needed after handshake
    }
}
