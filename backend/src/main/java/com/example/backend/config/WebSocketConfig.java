package com.example.backend.config;

import com.example.backend.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final JwtTokenProvider jwtTokenProvider;

    public WebSocketConfig(ChatWebSocketHandler chatWebSocketHandler, JwtTokenProvider jwtTokenProvider) {
        this.chatWebSocketHandler = chatWebSocketHandler;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(new WebSocketAuthInterceptor(jwtTokenProvider))
                .setAllowedOrigins("http://localhost:4200", "https://localhost:4200", "https://localhost:8443",
                        "https://localhost:443", "http://localhost:8443", "http://localhost:443");
    }
}