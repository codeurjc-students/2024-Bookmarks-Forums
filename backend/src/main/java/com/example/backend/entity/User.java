package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
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

    public interface UsernameInfo {
    }

    public interface BasicInfo extends UsernameInfo {
    }

    public interface FollowersInfo {
    }

    public interface CommunitiesInfo {
    }

    public interface BanInfo extends BasicInfo {
    }

    @JsonView(UsernameInfo.class)
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
    @ElementCollection(fetch = FetchType.EAGER) // Eager fetch because roles should be loaded when user is loaded
    private List<String> roles;

    @Setter
    @JsonIgnore // Do not serialize this field
    @Lob
    private Blob pfp;

    @JsonView(BanInfo.class)
    private int banCount = 0; // Accumulative, never decreases

    @JsonView(BasicInfo.class)
    private String pfpString;

    @JsonView(BasicInfo.class)
    private int followers = 0;

    @JsonView(BasicInfo.class)
    private int following = 0;

    @JsonView(BanInfo.class)
    private boolean isDisabled = false; // If true, user cannot log in

    @JsonView(BanInfo.class)
    private LocalDateTime disabledUntil;

    @JsonView(FollowersInfo.class)
    @ManyToMany
    private List<User> followersList = new ArrayList<>();

    @JsonView(FollowersInfo.class)
    @ManyToMany
    private List<User> followingList = new ArrayList<>();

    // A user may have upvoted 0 or multiple posts
    @ManyToMany(mappedBy = "upvotedBy")
    private List<Post> upvotedPosts = new ArrayList<>();

    // A user may have downvoted 0 or multiple posts
    @ManyToMany(mappedBy = "downvotedBy")
    private List<Post> downvotedPosts = new ArrayList<>();

    // A user may have liked 0 or multiple replies
    @ManyToMany(mappedBy = "likedBy")
    private List<Reply> likedReplies = new ArrayList<>();

    // A user can have 0 or multiple posts
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    // A user can have 0 or multiple replies
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Reply> replies = new ArrayList<>();

    // A user can join 0 or multiple communities
    @JsonView(CommunitiesInfo.class)
    @ManyToMany(mappedBy = "members")
    private List<Community> communities = new ArrayList<>();

    @JsonView(BasicInfo.class)
    private LocalDate creationDate = LocalDate.now();

    @JsonView(BasicInfo.class)
    private LocalTime creationTime = LocalTime.now();

    @JsonView(BasicInfo.class) // User creation date
    private LocalDateTime fullCreationDate = LocalDateTime.of(creationDate, creationTime);

    public User() {
    }

    public User(String username, String alias, String description, String pfp, String email, String password,
            List<String> roles) throws IOException, SQLException {
        this.username = username;
        this.alias = alias;
        this.description = description;
        this.roles = roles;
        this.email = email;
        this.password = password;
        if (pfp == null || pfp.isEmpty()) {
            this.pfpString = "/assets/defaultProfilePicture.png";
        } else {
            this.pfpString = pfp;
        }
        this.pfp = LocalImageToBlob(pfpString);
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

    public void addLikedReply(Reply reply) {
        this.likedReplies.add(reply);
    }

    public void removeLikedReply(Reply reply) {
        this.likedReplies.remove(reply);
    }

    public void addUpvotedPost(Post post) {
        this.upvotedPosts.add(post);
    }

    public void removeUpvotedPost(Post post) {
        this.upvotedPosts.remove(post);
    }

    public void addDownvotedPost(Post post) {
        this.downvotedPosts.add(post);
    }

    public void removeDownvotedPost(Post post) {
        this.downvotedPosts.remove(post);
    }

    public Blob LocalImageToBlob(String imgPath) throws IOException, SQLException {
        imgPath = imgPath.replace("/assets", "backend/src/main/resources/static/assets");
        String onDocker = System.getenv("RUNNING_IN_DOCKER");
        Blob imgBlob = null;

        if (onDocker != null && onDocker.equals("true")) {
            try (InputStream imgStream = getClass()
                    .getResourceAsStream(imgPath.replace("backend/src/main/resources/static", "/static"))) {
                if (imgStream == null) {
                    throw new IOException("Image not found");
                }
                imgBlob = new SerialBlob(imgStream.readAllBytes());
            }
        } else {
            String baseDir = System.getProperty("user.dir").replace("\\", "/").replace("/backend", "");
            File imgFile = new File(baseDir + "/" + imgPath);
            if (!imgFile.exists() || !imgFile.canRead()) {
                throw new IOException("Cannot access image file: " + imgFile.getAbsolutePath());
            }
            imgBlob = new SerialBlob(Files.readAllBytes(imgFile.toPath()));
        }
        return imgBlob;
    }

    public void addBanCount() {
        this.banCount++;
    }

    public void setDisabled(boolean isDisabled, LocalDateTime duration) {
        this.isDisabled = isDisabled;
        this.disabledUntil = duration;
    }

    public String toString() {
        return this.username;
    }

}
