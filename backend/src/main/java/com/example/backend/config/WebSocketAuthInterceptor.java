package com.example.backend.config;

import com.example.backend.security.jwt.JwtTokenProvider;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Map;

public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketAuthInterceptor(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Extract token from URL parameters
        String query = request.getURI().getQuery();
        String token = null;
        if (query != null && query.startsWith("token=")) {
            token = query.substring("token=".length());
            token = java.net.URLDecoder.decode(token, java.nio.charset.StandardCharsets.UTF_8);
        }

        // Validate token
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // Get authentication from token
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            if (auth != null) {
                // Store authentication in attributes to be used by WebSocket handler
                attributes.put("username", auth.getName());
                return true;
            }
        }

        return false; // Reject the handshake if token is invalid
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
        // Cleanup if needed
    }
} 