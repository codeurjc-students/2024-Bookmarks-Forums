package com.example.backend.repository;

import com.example.backend.entity.Chat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("SELECT c FROM Chat c WHERE c.user1.username = :username OR c.user2.username = :username ORDER BY c.lastMessageTime DESC")
    Page<Chat> findByUsername(@Param("username") String username, Pageable pageable);

    @Query("SELECT c FROM Chat c WHERE (c.user1.username = :user1 AND c.user2.username = :user2) OR (c.user1.username = :user2 AND c.user2.username = :user1)")
    Optional<Chat> findByUsers(@Param("user1") String user1, @Param("user2") String user2);
} 