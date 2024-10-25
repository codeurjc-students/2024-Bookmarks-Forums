package com.example.backend.service;

import com.example.backend.entity.Chat;
import com.example.backend.entity.Message;
import com.example.backend.repository.ChatRepository;
import com.example.backend.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    private final ChatRepository chatRepository;

    public MessageService(MessageRepository messageRepository, ChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.chatRepository = chatRepository;
    }

    public void saveMessage(Message message) {
        messageRepository.save(message);
    }

    public void deleteMessage(Message message) {
        if (message != null) {
            messageRepository.delete(message);
        }
    }

    public Message getMessageById(Long id) {
        return messageRepository.findByIdentifier(id);
    }

    public Page<Message> getMessagesBySenderUsername(String username, Pageable pageable) {
        return messageRepository.findBySenderUsername(username, pageable);
    }

    public Page<Message> getMessagesByChat(Long chatId, Pageable pageable) {
        Chat chat = chatRepository.findByIdentifier(chatId);
        if (chat != null) {
            return messageRepository.findByChat(chat, pageable);
        } else {
            return null;
        }
    }

    public void sendMessage(Message message) {
        Chat chat = message.getChat();
        chat.addMessage(message);
        chatRepository.save(chat);
    }
}
