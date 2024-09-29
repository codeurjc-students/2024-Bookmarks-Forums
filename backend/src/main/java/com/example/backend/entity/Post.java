package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
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

    public interface UserInfo {
    }

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    @JsonView(BasicInfo.class)
    private Long identifier;

    @JsonView(UserInfo.class)
    @ManyToOne
    private User author;

    @JsonView(BasicInfo.class)
    private String title;

    @JsonView(BasicInfo.class)
    private String content;

    @JsonView(BasicInfo.class)
    private String communityNameString;

    // A post can only be made in one community
    @ManyToOne
    @JsonView(CommunityInfo.class)
    private Community community;

    @JsonView(DetailedInfo.class)
    private int upvotes;

    @JsonView(DetailedInfo.class)
    private int downvotes;

    @JsonView(DetailedInfo.class) // Number of comments
    private int comments;

    @JsonView(BasicInfo.class)
    private LocalDate creationDate = LocalDate.now();

    @JsonView(BasicInfo.class)
    private LocalTime creationTime = LocalTime.now();

    @JsonView(BasicInfo.class) // Post creation date
    private LocalDateTime fullCreationDate = LocalDateTime.of(creationDate, creationTime);

    @JsonView(BasicInfo.class)
    private LocalDate lastReplyDate = LocalDate.now();

    @JsonView(BasicInfo.class)
    private LocalTime lastReplyTime = LocalTime.now();

    @JsonView(BasicInfo.class) // Post modification date (last reply)
    private LocalDateTime fullLastReplyDate = LocalDateTime.of(lastReplyDate, lastReplyTime);

    @JsonView(Replies.class) // List of comments
    @OneToMany(mappedBy = "post", cascade = jakarta.persistence.CascadeType.ALL)
    private List<Reply> replyList = new ArrayList<>();

    public Post() {
    }

    public Post(String title, String content, User author, Community community) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.community = community;
        this.communityNameString = community.getName();
    }

}
