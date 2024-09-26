package com.example.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import javax.sql.rowset.serial.SerialBlob;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
public class User {

    public interface BasicInfo {
    }

    @JsonView(BasicInfo.class)
    @Id
    private String username;

    @JsonView(BasicInfo.class)
    private String alias;

    @JsonView(BasicInfo.class)
    private String description;

    @JsonView(BasicInfo.class)
    @ElementCollection
    private List<String> roles;

    @JsonIgnore // Do not serialize this field
    @Lob
    private Blob pfp;

    // A user can have 0 or multiple posts
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    // A user can have 0 or multiple replies
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL)
    private List<Reply> replies = new ArrayList<>();

    // A user can join 0 or multiple communities
    @ManyToMany(mappedBy = "users")
    private List<Community> communities = new ArrayList<>();

    // A user can have 0 or multiple chats
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Chat> chats = new ArrayList<>();

    // A user may have sent 0 or multiple messages
    @JsonIgnore
    @OneToMany(mappedBy = "sender", cascade = CascadeType.ALL)
    private List<Message> sentMessages = new ArrayList<>();

    public User() {
    }

    public User(String username, String alias, String description, List<String> roles, String pfp) throws SQLException {
        this.username = username;
        this.alias = alias;
        this.description = description;
        this.roles = roles;
        if (pfp != null) {
            this.pfp = new SerialBlob(pfp.getBytes());
        }
    }

    public void setPfp(String pfp) throws SQLException {
        this.pfp = new SerialBlob(pfp.getBytes());
    }

    public String toString() {
        return this.username;
    }
}

