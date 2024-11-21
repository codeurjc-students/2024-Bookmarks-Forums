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
import java.util.Objects;

@Setter
@Getter
@Entity
public class Community {

    public interface NameInfo {
    }

    public interface BasicInfo extends NameInfo {
    }

    public interface UsersInfo {
    }

    // The community ID is the same as the community name but with spaces replaced
    // by underscores
    @Id
    @JsonView(NameInfo.class)
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    private Long identifier;

    @JsonView(NameInfo.class)
    private String name;

    @JsonView(BasicInfo.class)
    @Column(length = 500)
    private String description;

    @JsonView(BasicInfo.class)
    @ManyToOne
    private User admin;

    // Moderators list
    @JsonView(UsersInfo.class)
    @ManyToMany
    @JoinTable(name = "moderator_community", joinColumns = @JoinColumn(name = "community_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> moderators = new ArrayList<>();

    @JsonView(BasicInfo.class)
    private boolean hasBanner = false;

    // Banner image
    @JsonIgnore
    @Lob
    private Blob banner;

    @JsonView(BasicInfo.class)
    private String bannerString;

    // A community can have multiple users
    @ManyToMany
    @JsonView(UsersInfo.class)
    @JoinTable(name = "user_community", joinColumns = @JoinColumn(name = "community_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
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

    @JsonView(UsersInfo.class) // banned users List of Ban Entities
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL)
    private List<Ban> bannedUsers = new ArrayList<>();

    public Community() {
    }

    public Community(String name, String description, String banner, User admin) {
        this.name = name;
        this.members.add(admin);
        this.admin = admin;
        this.description = description;
        this.bannerString = Objects.requireNonNullElse(banner, "default_community_banner.jpg");
    }

    public void addMember(User user) {
        this.members.add(user);
    }

    public void removeMember(User user) {
        this.members.remove(user);
    }

    public void addModerator(User user) {
        this.moderators.add(user);
    }

    public void removeModerator(User user) {
        this.moderators.remove(user);
    }

    public String toString() {
        return this.name;
    }

    public void setBanner(Blob banner) {
        this.banner = banner;
        if (banner != null) {
            this.hasBanner = true;
        } else {
            this.hasBanner = false;
        }
    }

    public void banUser(User user, LocalDateTime duration, String reason) {
        Ban ban = new Ban(user, this, reason, duration);
        this.bannedUsers.add(ban);
    }

    public void unbanUser(Long banID) {
        if (this.bannedUsers.size() > 0) {
            for (Ban ban : this.bannedUsers) {
                if (ban.getId().equals(banID)) {
                    this.bannedUsers.remove(ban);
                    break;
                }
            }
        }
    }

    public void unbanUserByUser(User user) {
        if (this.bannedUsers.size() > 0) {
            for (Ban ban : this.bannedUsers) {
                if (ban.getUser().equals(user)) {
                    this.bannedUsers.remove(ban);
                    break;
                }
            }
        }
    }
}
