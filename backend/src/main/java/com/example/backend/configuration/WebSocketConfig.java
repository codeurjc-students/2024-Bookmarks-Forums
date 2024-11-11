package com.example.backend.configuration;

import com.example.backend.component.WebSocketChatHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketChatHandler webSocketChatHandler;
    private final WebSocketHandshakeInterceptor webSocketHandshakeInterceptor;

    @Autowired
    public WebSocketConfig(WebSocketChatHandler webSocketChatHandler, WebSocketHandshakeInterceptor webSocketHandshakeInterceptor) {
        this.webSocketChatHandler = webSocketChatHandler;
        this.webSocketHandshakeInterceptor = webSocketHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketChatHandler, "/chat")
                .setAllowedOrigins("*")
                .addInterceptors(webSocketHandshakeInterceptor);
    }
}