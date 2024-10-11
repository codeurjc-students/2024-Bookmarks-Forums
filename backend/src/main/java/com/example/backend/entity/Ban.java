package com.example.backend.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.persistence.*;

@Entity
public class Ban {

    public interface BasicInfo extends User.UsernameInfo, Community.NameInfo {
    }

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.AUTO)
    @JsonView(BasicInfo.class)
    private Long id;

    @OneToOne
    @JsonView(User.UsernameInfo.class)
    @JoinColumn(name = "username")
    private User user;

    @ManyToOne
    @JsonView(Community.NameInfo.class)
    @JoinColumn(name = "community_id")
    private Community community;

    @JsonView(BasicInfo.class)
    private String banReason;

    @JsonView(BasicInfo.class)
    private LocalDateTime banUntil;

    public Ban() {
    }

    public Ban(User user, Community community, String banReason, LocalDateTime banUntil) {
        this.user = user;
        this.community = community;
        this.banReason = banReason;
        this.banUntil = banUntil;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBanReason() {
        return banReason;
    }

    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }

    public LocalDateTime getBanUntil() {
        return banUntil;
    }

    public void setBanUntil(LocalDateTime banUntil) {
        this.banUntil = banUntil;
    }
    
}
