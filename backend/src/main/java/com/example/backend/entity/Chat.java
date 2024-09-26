package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
public class Chat {

    public interface BasicInfo {
    }

    public interface Messages {
    }

    public interface UserInfo {
    }

    @Id
    @JsonView(BasicInfo.class)
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    private Long id;

    // A chat can only be between two users
    @ManyToOne
    @JsonView(UserInfo.class)
    private User user1;

    @ManyToOne
    @JsonView(UserInfo.class)
    private User user2;

    // A chat has multiple messages
    @JsonView(Messages.class)
    @OneToMany(mappedBy = "chat")
    private List<Message> messages;

    public Chat() {
    }

    public Chat(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

}
