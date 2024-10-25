package com.example.backend.service;

import com.example.backend.entity.Chat;
import com.example.backend.repository.ChatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Page<Chat> getAllChats(Pageable pageable, boolean sorted) {
        if (sorted) {
            return chatRepository.findChatsOrderByLastMessageDate(pageable);
        } else {
            return chatRepository.findAll(pageable);
        }
    }

    public Chat getChatById(Long id) {
        return chatRepository.findByIdentifier(id);
    }

    public Page<Chat> getChatByName(String name, String username, boolean sorted, Pageable pageable) {
        if (sorted) {
            return chatRepository.findByNameAndUserOrderByLastMessageDate(name, username, pageable);
        } else {
            return chatRepository.findByNameAndUser(name, username, pageable);
        }
    }

    public Page<Chat> getChatByUser(String username, boolean sorted, Pageable pageable) {
        if (sorted) {
            return chatRepository.findByUserOrderByLastMessageDate(username, pageable);
        } else {
            return chatRepository.findByUser(username, pageable);
        }
    }

    public void saveChat(Chat chat) {
        chatRepository.save(chat);
    }

    public void deleteChat(Chat chat) {
        if (chat != null) {
            chatRepository.delete(chat);
        }
    }

    public void leaveChat(Chat chat, String username) {
        if (chat != null) {
            if (chat.getUser1().getUsername().equals(username)) {
                chat.setUser1(null);
            } else if (chat.getUser2().getUsername().equals(username)) {
                chat.setUser2(null);
            }
            chatRepository.save(chat);
        }
    }


}
