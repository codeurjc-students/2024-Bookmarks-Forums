package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Setter
@Getter
@Entity
public class Message {

    public interface BasicInfo {
    }

    public interface ChatInfo {
    }

    @Id
    @JsonView(BasicInfo.class)
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    private Long identifier;

    @JsonView(BasicInfo.class)
    private String content;

    // Sender
    @ManyToOne
    @JsonView(BasicInfo.class)
    private User sender;

    // Chat ID to which the message belongs
    @ManyToOne
    @JsonView(ChatInfo.class)
    private Chat chat;

    @JsonView(BasicInfo.class)
    private LocalDate creationDate = LocalDate.now();

    @JsonView(BasicInfo.class)
    private LocalTime creationTime = LocalTime.now();

    @JsonView(BasicInfo.class) // Message creation date
    private LocalDateTime fullCreationDate = LocalDateTime.of(creationDate, creationTime);

    public Message() {
    }

    public Message(String content, User sender, Chat chat) {
        this.content = content;
        this.sender = sender;
        this.chat = chat;
    }

    @Override
    public String toString() {
        return "Message{" +
                "identifier=" + identifier +
                ", content='" + content + '\'' +
                ", sender=" + sender +
                ", chat=" + chat +
                '}';
    }


}
