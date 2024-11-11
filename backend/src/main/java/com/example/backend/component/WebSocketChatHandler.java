package com.example.backend.component;

import com.example.backend.entity.Chat;
import com.example.backend.entity.Message;
import com.example.backend.entity.User;
import com.example.backend.service.ChatService;
import com.example.backend.service.MessageService;
import com.example.backend.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.example.backend.exception.PayloadParsingException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketChatHandler extends TextWebSocketHandler {

    private final ChatService chatService;
    private final MessageService messageService;
    private final UserService userService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebSocketChatHandler(ChatService chatService, MessageService messageService, UserService userService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.userService = userService;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) throws Exception {
        String username = (String) session.getAttributes().get("username");
        if (username != null) {
            sessions.put(username, session);
        } else {
            session.close(CloseStatus.BAD_DATA);
            throw new IllegalArgumentException("Username is null in WebSocket session attributes");
        }
    }

    @Override
    protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, String> messageData = parseMessagePayload(payload);

        Long chatId = Long.parseLong(messageData.get("chatId"));
        String senderUsername = messageData.get("sender");
        String content = messageData.get("content");

        User sender = userService.getUserByUsername(senderUsername);
        Chat chat = chatService.getChatById(chatId);

        if (sender != null && chat != null) {
            Message newMessage = new Message(content, sender, chat);
            messageService.saveMessage(newMessage);

            // Broadcast the message to all participants in the chat
            for (User participant : List.of(chat.getUser1(), chat.getUser2())) {
                WebSocketSession participantSession = sessions.get(participant.getUsername());
                if (participantSession != null && participantSession.isOpen()) {
                    participantSession.sendMessage(new TextMessage(payload));
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) throws Exception {
        String username = (String) session.getAttributes().get("username");
        sessions.remove(username);
    }

    private Map<String, String> parseMessagePayload(String payload) throws PayloadParsingException {
        try {
            return objectMapper.readValue(payload, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new PayloadParsingException("Failed to parse message payload", e);
        }
    }
}