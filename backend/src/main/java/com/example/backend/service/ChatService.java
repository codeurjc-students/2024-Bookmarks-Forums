package com.example.backend.service;

import com.example.backend.config.ChatWebSocketHandler.ChatMessage;
import com.example.backend.entity.Chat;
import com.example.backend.entity.Message;
import com.example.backend.entity.User;
import com.example.backend.repository.ChatRepository;
import com.example.backend.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class ChatService {
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final UserService userService;

    public ChatService(ChatRepository chatRepository, MessageRepository messageRepository, UserService userService) {
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    @Transactional
    public void saveMessage(ChatMessage chatMessage) {
        User sender = userService.getUserByUsername(chatMessage.getSenderUsername());
        User recipient = userService.getUserByUsername(chatMessage.getRecipientUsername());

        if (sender == null || recipient == null) {
            throw new IllegalArgumentException("Invalid sender or recipient");
        }

        // Find or create chat
        Chat chat = chatRepository.findByUsers(sender.getUsername(), recipient.getUsername())
                .orElseGet(() -> new Chat(sender, recipient));

        // Create and save message
        Message message = new Message(sender, recipient, chatMessage.getContent());
        message.setTimestamp(LocalDateTime.ofInstant(
            Instant.ofEpochMilli(chatMessage.getTimestamp()), 
            ZoneId.systemDefault()
        ));
        
        // Add message to chat
        chat.addMessage(message);
        
        // Save the chat which will cascade save the message
        chatRepository.save(chat);
    }

    public Page<Chat> getUserChats(String username, Pageable pageable) {
        System.out.println("Getting chats for user: " + username);
        Page<Chat> chats = chatRepository.findByUsername(username, pageable);
        
        // Calculate unread counts for each chat
        if (chats != null) {
            chats.getContent().forEach(chat -> {
                chat.setUnreadCount(chat.calculateUnreadCount(username));
            });
        }
        
        System.out.println("Found " + (chats != null ? chats.getContent().size() : 0) + " chats");
        if (chats != null && !chats.getContent().isEmpty()) {
            System.out.println("First chat id: " + chats.getContent().get(0).getId());
        }
        return chats;
    }

    @Transactional(readOnly = true)
    public List<Message> getChatMessages(Long chatId, String username) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        // Verify user has access to this chat
        if (!chat.getUser1().getUsername().equals(username) && 
            !chat.getUser2().getUsername().equals(username)) {
            throw new IllegalArgumentException("User not authorized to access this chat");
        }

        // Force loading of messages
        List<Message> messages = chat.getMessages();
        messages.size(); // Force initialization of the lazy collection
        return messages;
    }

    @Transactional
    public void markMessagesAsRead(Long chatId, String username) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        if (!chat.getUser1().getUsername().equals(username) && 
            !chat.getUser2().getUsername().equals(username)) {
            throw new IllegalArgumentException("User not authorized to access this chat");
        }

        // Mark all messages sent to this user as read
        for (Message message : chat.getMessages()) {
            if (message.getReceiver().getUsername().equals(username) && !message.isRead()) {
                message.setRead(true);
            }
        }

        chatRepository.save(chat);
    }

    public Long getUnreadMessageCount(String username) {
        return messageRepository.countUnreadMessagesByUsername(username);
    }
} 