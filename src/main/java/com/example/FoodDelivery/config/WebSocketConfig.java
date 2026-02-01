package com.example.FoodDelivery.config;

import java.security.Principal;
import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor authInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to send messages to clients
        // /topic for broadcast messages, /queue for user-specific messages
        config.enableSimpleBroker("/topic", "/queue");
        // Set prefix for messages from client to server
        config.setApplicationDestinationPrefixes("/app");
        // Set prefix for user-specific destinations (e.g., /user/queue/orders)
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS fallback and authentication
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(authInterceptor)
                .setHandshakeHandler(new CustomHandshakeHandler())
                .withSockJS();

        // Register native WebSocket endpoint (without SockJS) for testing with wscat
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(authInterceptor)
                .setHandshakeHandler(new CustomHandshakeHandler());
    }

    /**
     * Custom handshake handler to set the Principal (user identity) from the
     * authenticated email stored in session attributes by the interceptor
     */
    private static class CustomHandshakeHandler extends DefaultHandshakeHandler {
        @Override
        protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                Map<String, Object> attributes) {
            // Get email from attributes (set by WebSocketAuthInterceptor)
            String email = (String) attributes.get("email");
            if (email != null) {
                // Return a Principal with the email as the name
                return () -> email;
            }
            return null;
        }
    }
}
