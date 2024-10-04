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
public class Community {

    public interface BasicInfo {
    }

    public interface UsersInfo {
    }

    // The community ID is the same as the community name but with spaces replaced by underscores
    @Id
    @JsonView(BasicInfo.class)
    private String identifier;

    @JsonView(BasicInfo.class)
    private String name;

    @JsonView(BasicInfo.class)
    private String description;

    @JsonView(BasicInfo.class)
    @ManyToOne
    private User admin;

    // Banner image
    @JsonIgnore
    @Lob
    private Blob banner;

    @JsonView(BasicInfo.class)
    private String bannerString;

    // A community can have multiple users
    @ManyToMany
    @JsonView(UsersInfo.class)
    private List<User> members = new ArrayList<>();

    // A community can have multiple posts
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    @JsonView(BasicInfo.class)
    private LocalDate creationDate = LocalDate.now();

    @JsonView(BasicInfo.class)
    private LocalTime creationTime = LocalTime.now();

    @JsonView(BasicInfo.class) // Creation date
    private LocalDateTime fullCreationDate = LocalDateTime.of(creationDate, creationTime);

    @JsonView(BasicInfo.class)
    private LocalDate lastPostDate = LocalDate.now();

    @JsonView(BasicInfo.class)
    private LocalTime lastPostTime = LocalTime.now();

    @JsonView(BasicInfo.class) // Modification date (last post)
    private LocalDateTime fullLastPostDate = LocalDateTime.of(lastPostDate, lastPostTime);

    public Community() {
    }

    public Community(String name, String description, String banner, User admin) {
        this.name = name;
        this.admin = admin;
        this.members.add(admin);
        this.identifier = name.replace(" ", "_");
        this.description = description;
        this.bannerString = Objects.requireNonNullElse(banner, "default_community_banner.jpg");
    }

    public void addMember(User user) {
        this.members.add(user);
    }

    public void removeMember(User user) {
        this.members.remove(user);
    }

    public String toString() {
        return this.name;
    }


}
