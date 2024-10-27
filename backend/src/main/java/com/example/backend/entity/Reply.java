package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Reply {

    public interface LikesInfo {
    }

    public interface PostInfo {
    }

    public interface UserInfo {
    }

    public interface BasicInfo extends Post.IdInfo, UserInfo, PostInfo {
    }

    @Id
    @JsonView(BasicInfo.class)
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

    // Users who liked the reply (a reply can be liked by multiple users)
    @JsonView(LikesInfo.class)
    @ManyToMany
    @JoinTable(name = "reply_liked_by", joinColumns = @JoinColumn(name = "reply_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> likedBy = new ArrayList<>();

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

    public void addLikedBy(User user) {
        likedBy.add(user);
    }

    public void removeLikedBy(User user) {
        likedBy.remove(user);
    }
}
