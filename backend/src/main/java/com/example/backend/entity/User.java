package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Setter
@Getter
@Entity
public class User {

    public interface BasicInfo {
    }

    public interface FollowersInfo {
    }

    @JsonView(BasicInfo.class)
    @Id
    private String username;

    @JsonIgnore
    private String password;

    @JsonView(BasicInfo.class)
    private String alias;

    @JsonView(BasicInfo.class)
    private String email;

    @JsonView(BasicInfo.class)
    private String description;

    @Getter
    @JsonView(BasicInfo.class)
    @ElementCollection
    private List<String> roles;

    @Setter
    @JsonIgnore // Do not serialize this field
    @Lob
    private Blob pfp;

    @JsonView(BasicInfo.class)
    private String pfpString;

    @JsonView(BasicInfo.class)
    private int followers = 0;

    @JsonView(BasicInfo.class)
    private int following = 0;

    @JsonView(FollowersInfo.class)
    @ManyToMany
    private List<User> followersList = new ArrayList<>();

    @JsonView(FollowersInfo.class)
    @ManyToMany
    private List<User> followingList = new ArrayList<>();

    // A user can have 0 or multiple posts
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    // A user can have 0 or multiple replies
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Reply> replies = new ArrayList<>();

    // A user can join 0 or multiple communities
    @ManyToMany(mappedBy = "members")
    private List<Community> communities = new ArrayList<>();

    // A user can have 0 or multiple chats
    @OneToMany(mappedBy = "user1", cascade = CascadeType.ALL)
    private List<Chat> chats1 = new ArrayList<>();

    @OneToMany(mappedBy = "user2", cascade = CascadeType.ALL)
    private List<Chat> chats2 = new ArrayList<>();

    // A user may have sent 0 or multiple messages
    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Message> sentMessages = new ArrayList<>();

    @JsonView(BasicInfo.class)
    private LocalDate creationDate = LocalDate.now();

    @JsonView(BasicInfo.class)
    private LocalTime creationTime = LocalTime.now();

    @JsonView(BasicInfo.class) // User creation date
    private LocalDateTime fullCreationDate = LocalDateTime.of(creationDate, creationTime);

    public User() {
    }

    public User(String username, String alias, String description, String pfp, String email, String password, List<String> roles) {
        this.username = username;
        this.alias = alias;
        this.description = description;
        this.roles = roles;
        this.email = email;
        this.password = password;
        this.pfpString = Objects.requireNonNullElse(pfp, "/assets/defaultProfilePicture.png");
    }

    public void addFollower(User user) {
        this.followersList.add(user);
        this.followers++;
    }

    public void removeFollower(User user) {
        this.followersList.remove(user);
        this.followers--;
    }

    public void addFollowing(User user) {
        this.followingList.add(user);
        this.following++;
    }

    public void removeFollowing(User user) {
        this.followingList.remove(user);
        this.following--;
    }

    public String toString() {
        return this.username;
    }
}

