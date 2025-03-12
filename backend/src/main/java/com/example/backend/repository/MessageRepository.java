package com.example.backend.repository;

import com.example.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.username = :username AND m.read = false")
    Long countUnreadMessagesByUsername(@Param("username") String username);
} 