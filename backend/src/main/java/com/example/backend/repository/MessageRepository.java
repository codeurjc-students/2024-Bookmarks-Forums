package com.example.backend.repository;

import com.example.backend.entity.Chat;
import com.example.backend.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    Message findByIdentifier(Long messageId);

    // Find messages with a given username as a sender
    Page<Message> findBySenderUsername(String username, Pageable pageable);

    // Find message by content
    Message findByContent(String content);

    // Find messages with a given chat
    Page<Message> findByChat(Chat chat, Pageable pageable);

}
