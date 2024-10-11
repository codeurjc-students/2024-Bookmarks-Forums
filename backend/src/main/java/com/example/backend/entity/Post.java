package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Post {

    public interface BasicInfo {
    }

    public interface DetailedInfo extends BasicInfo {
    }

    public interface Replies {

    }

    public interface CommunityInfo {
    }

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    @JsonView(BasicInfo.class)
    private Long identifier;

    @JsonView(BasicInfo.class)
    @ManyToOne
    private User author;

    @JsonView(BasicInfo.class)
    private String title;

    @JsonView(BasicInfo.class)
    private String content;

    @JsonView(BasicInfo.class)
    private String communityNameString;

    @JsonIgnore
    @Lob
    private Blob image;

    // A post can only be made in one community
    @ManyToOne
    @JsonView(CommunityInfo.class)
    private Community community;

    @JsonView(BasicInfo.class)
    private int upvotes;

    @JsonView(BasicInfo.class)
    private int downvotes;

    @JsonView(BasicInfo.class) // Number of comments
    private int comments;

    @JsonView(DetailedInfo.class)
    private LocalDate creationDate = LocalDate.now();

    @JsonView(DetailedInfo.class)
    private LocalTime creationTime = LocalTime.now();

    @JsonView(BasicInfo.class) // Post creation date
    private LocalDateTime fullCreationDate = LocalDateTime.of(creationDate, creationTime);

    @JsonView(DetailedInfo.class)
    private LocalDate lastReplyDate = LocalDate.now();

    @JsonView(DetailedInfo.class)
    private LocalTime lastReplyTime = LocalTime.now();

    @JsonView(BasicInfo.class) // Post last reply date
    private LocalDateTime fullLastReplyDate = LocalDateTime.of(lastReplyDate, lastReplyTime);

    @JsonView(DetailedInfo.class)
    private boolean isEdited = false;

    @JsonView(DetailedInfo.class)
    private LocalDate lastEditDate = LocalDate.now();

    @JsonView(DetailedInfo.class)
    private LocalTime lastEditTime = LocalTime.now();

    @JsonView(BasicInfo.class) // Post last edit date
    private LocalDateTime fullLastEditDate = LocalDateTime.of(lastEditDate, lastEditTime);

    @JsonView(Replies.class) // List of comments
    @OneToMany(mappedBy = "post", cascade = jakarta.persistence.CascadeType.ALL)
    private List<Reply> replyList = new ArrayList<>();

    public Post() {
    }

    public void upvote() {
        this.upvotes++;
    }

    public void downvote() {
        this.downvotes++;
    }

    public void addImage(Blob image) {
        this.image = image;
    }

    public Post(String title, String content, User author, Community community) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.community = community;
        this.communityNameString = community.getName();
    }

}
