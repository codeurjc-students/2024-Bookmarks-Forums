package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "messages")
public class Message {
    public interface BasicInfoForChatList {}
    public interface BasicInfo extends BasicInfoForChatList {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(BasicInfo.class)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    @JsonView(BasicInfoForChatList.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    @JsonView(BasicInfo.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User receiver;

    @Column(nullable = false)
    @JsonView(BasicInfoForChatList.class)
    private String content;

    @Column(name = "is_read")
    @JsonView(BasicInfo.class)
    private boolean read;

    @Column(nullable = false)
    @JsonView(BasicInfo.class)
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    @JsonView(BasicInfo.class)
    @JsonIgnore
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Chat chat;

    // Constructors
    public Message() {
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }

    public Message(User sender, User receiver, String content) {
        this();
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }
} 