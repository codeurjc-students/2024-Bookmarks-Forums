package com.example.backend.service;

import com.example.backend.entity.Chat;
import com.example.backend.entity.Message;
import com.example.backend.entity.User;
import com.example.backend.repository.ChatRepository;
import com.example.backend.repository.MessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Blob;

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

    public Message getMessageByContent(String content) {
        return messageRepository.findByContent(content);
    }

    public void deleteMessageById(Long id) {
        messageRepository.deleteById(id);
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

    public void removeMessage(Message message) {
        Chat chat = message.getChat();
        chat.removeMessage(message);
        chatRepository.save(chat);
    }

    public void sendMessageImage(Message message, Blob image) {
        message.addImage(image);
        messageRepository.save(message);
    }

    public User getSender(Message message) {
        return message.getSender();
    }

    public Chat getChat(Message message) {
        return message.getChat();
    }
}
