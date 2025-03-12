package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonView;

@Entity
@Table(name = "chats")
public class Chat {
    public interface BasicInfo {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(BasicInfo.class)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    @JsonView(BasicInfo.class)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    @JsonView(BasicInfo.class)
    private User user2;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "chat_id")
    @OrderBy("timestamp DESC")
    @JsonView(BasicInfo.class)
    private List<Message> messages = new ArrayList<>();

    @Column(nullable = false)
    @JsonView(BasicInfo.class)
    private LocalDateTime lastMessageTime;

    // Constructors
    public Chat() {
        this.lastMessageTime = LocalDateTime.now();
    }

    public Chat(User user1, User user2) {
        this();
        this.user1 = user1;
        this.user2 = user2;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser1() {
        return user1;
    }

    public void setUser1(User user1) {
        this.user1 = user1;
    }

    public User getUser2() {
        return user2;
    }

    public void setUser2(User user2) {
        this.user2 = user2;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
        this.lastMessageTime = message.getTimestamp();
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
} 