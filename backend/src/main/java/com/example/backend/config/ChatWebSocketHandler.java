package com.example.backend.config;

import com.example.backend.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final ChatService chatService;

    public ChatWebSocketHandler(ObjectMapper objectMapper, ChatService chatService) {
        this.objectMapper = objectMapper;
        this.chatService = chatService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        System.out.println("WebSocket connection established for user: " + username);
        sessions.put(username, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String authenticatedUsername = (String) session.getAttributes().get("username");
        if (authenticatedUsername == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        System.out.println("Received message: " + message.getPayload());
        try {
            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            
            // Verify the sender is the authenticated user
            if (!authenticatedUsername.equals(chatMessage.getSenderUsername())) {
                session.close(CloseStatus.POLICY_VIOLATION);
                return;
            }

            System.out.println("Parsed message: sender=" + chatMessage.getSenderUsername() 
                + ", recipient=" + chatMessage.getRecipientUsername());
            
            chatService.saveMessage(chatMessage);

            // Send to recipient if online
            WebSocketSession recipientSession = sessions.get(chatMessage.getRecipientUsername());
            if (recipientSession != null && recipientSession.isOpen()) {
                recipientSession.sendMessage(message);
                System.out.println("Message forwarded to recipient: " + chatMessage.getRecipientUsername());
            } else {
                System.out.println("Recipient not online or session closed: " + chatMessage.getRecipientUsername());
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            System.out.println("WebSocket connection closed for user: " + username + " with status: " + status);
            sessions.remove(username);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket transport error: " + exception.getMessage());
        exception.printStackTrace();
        session.close(CloseStatus.SERVER_ERROR);
    }

    public static class ChatMessage {
        private String senderUsername;
        private String recipientUsername;
        private String content;
        private long timestamp;

        // Getters and setters
        public String getSenderUsername() {
            return senderUsername;
        }

        public void setSenderUsername(String senderUsername) {
            this.senderUsername = senderUsername;
        }

        public String getRecipientUsername() {
            return recipientUsername;
        }

        public void setRecipientUsername(String recipientUsername) {
            this.recipientUsername = recipientUsername;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
} 