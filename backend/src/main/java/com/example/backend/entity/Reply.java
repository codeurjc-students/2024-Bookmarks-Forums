package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonView;
import com.zaxxer.hikari.util.ClockSource;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
public class Reply {

    public interface BasicInfo {
    }

    public interface PostInfo {
    }

    public interface UserInfo {
    }

    @JsonView(BasicInfo.class)
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    private Long identifier;

    @ManyToOne
    @JsonView(PostInfo.class)
    private Post post;

    @JsonView(BasicInfo.class)
    private String title;

    @JsonView(BasicInfo.class)
    private String content;

    @JsonView(BasicInfo.class)
    private int likes = 0;

    @JsonView(UserInfo.class)
    @ManyToOne
    private User author;

    @JsonView(BasicInfo.class)
    private LocalDate creationDate = LocalDate.now();

    @JsonView(BasicInfo.class)
    private LocalTime creationTime = LocalTime.now();

    @JsonView(BasicInfo.class) // Reply creation date
    private LocalDateTime fullCreationDate = LocalDateTime.of(creationDate, creationTime);

    public Reply() {
    }

    public Reply(String title, String content, User author, Post post) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.post = post;
    }

    @Override
    public String toString() {
        return "Reply{" +
                "identifier=" + identifier +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", likes=" + likes +
                ", author=" + author +
                ", creationDate=" + creationDate +
                ", creationTime=" + creationTime +
                ", fullCreationDate=" + fullCreationDate +
                '}';
    }
}
