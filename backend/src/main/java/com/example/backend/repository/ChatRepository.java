package com.example.backend.repository;

import com.example.backend.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Chat findByIdentifier(Long id);

    Page<Chat> findByName(String name, Pageable pageable);

    // Find chats with a given username as a member (regardless of it's the user1 or user2)
    @Query("SELECT c FROM Chat c WHERE c.user1.username LIKE %:username% OR c.user2.username LIKE %:username%")
    Page<Chat> findByUser(String username, Pageable pageable);

    // Find and sort chats by latest updated date (lastMessageDate)
    @Query("SELECT c FROM Chat c ORDER BY c.fullLastMessageDate DESC")
    Page<Chat> findChatsOrderByLastMessageDate(Pageable pageable);

    // Find chats with a given username as a member (regardless of it's the user1 or user2) and sort by latest updated date (lastMessageDate)
    @Query("SELECT c FROM Chat c WHERE c.user1.username LIKE %:username% OR c.user2.username LIKE %:username% ORDER BY c.fullLastMessageDate DESC")
    Page<Chat> findByUserOrderByLastMessageDate(String username, Pageable pageable);

    // Find chats with a given title and sort by latest updated date (lastMessageDate)
    @Query("SELECT c FROM Chat c WHERE c.name LIKE %:name% ORDER BY c.fullLastMessageDate DESC")
    Page<Chat> findByNameOrderByLastMessageDate(String name, Pageable pageable);
}
