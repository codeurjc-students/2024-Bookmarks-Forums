package com.example.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonView;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "chats")
public class Chat {
    public interface BasicInfo {}
    public interface BasicInfoForChatList extends BasicInfo {}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(BasicInfo.class)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user1_id", nullable = false)
    @JsonView(BasicInfo.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user1;

    @ManyToOne
    @JoinColumn(name = "user2_id", nullable = false)
    @JsonView(BasicInfo.class)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user2;

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("timestamp DESC")
    @JsonView(BasicInfo.class)
    private List<Message> messages = new ArrayList<>();

    @OneToOne
    @JoinColumn(name = "last_message_id")
    @JsonView(BasicInfoForChatList.class)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Message lastMessage;

    @Column(nullable = false)
    @JsonView(BasicInfoForChatList.class)
    private LocalDateTime lastMessageTime;

    @Transient
    @JsonView(BasicInfoForChatList.class)
    private Long unreadCount;

    // Constructors
    public Chat() {
        this.lastMessageTime = LocalDateTime.now();
    }

    public Chat(User user1, User user2) {
        this();
        this.user1 = user1;
        this.user2 = user2;
    }

    // Calculate unread messages for a specific user
    public Long calculateUnreadCount(String username) {
        return messages.stream()
            .filter(message -> message.getReceiver().getUsername().equals(username) && !message.isRead())
            .count();
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
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
        if (messages != null) {
            this.messages.clear();
            this.messages.addAll(messages);
            // Update bidirectional relationships
            this.messages.forEach(message -> message.setChat(this));
            
            // Update last message if the list is not empty
            if (!this.messages.isEmpty()) {
                this.lastMessage = this.messages.get(0); // First message is the most recent due to OrderBy
                this.lastMessageTime = this.lastMessage.getTimestamp();
            }
        }
    }

    public void addMessage(Message message) {
        if (message != null) {
            this.messages.add(message);
            message.setChat(this);  // Set the bidirectional relationship
            this.lastMessage = message;
            this.lastMessageTime = message.getTimestamp();
        }
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(LocalDateTime lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }
} 