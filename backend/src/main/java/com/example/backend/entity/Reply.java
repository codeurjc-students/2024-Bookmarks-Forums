package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

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
    private Long id;

    @ManyToOne
    @JsonView(PostInfo.class)
    private Post post;

    @JsonView(BasicInfo.class)
    private String title;

    @JsonView(BasicInfo.class)
    private String content;

    @JsonView(BasicInfo.class)
    private String authorUsername;

    @JsonView(UserInfo.class)
    @ManyToOne
    private User author;

    public Reply() {
    }

    public Reply(String title, String content, User author, Post post) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.authorUsername = author.getUsername();
        this.post = post;
    }

    @Override
    public String toString() {
        return "Reply{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", authorUsername='" + authorUsername + '\'' +
                '}';
    }
}
