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
public class Community {

    public interface BasicInfo {
    }

    public interface UsersInfo {
    }

    // The community id is the same as the community name but with spaces replaced by underscores
    @Id
    @JsonView(BasicInfo.class)
    private String id;

    @JsonView(BasicInfo.class)
    private String name;

    @JsonView(BasicInfo.class)
    private String description;

    // Banner image
    @JsonIgnore
    @Lob
    private Blob banner;

    // A community can have multiple users
    @ManyToMany
    @JsonView(UsersInfo.class)
    private List<User> users = new ArrayList<>();

    // A community can have multiple posts
    @OneToMany(mappedBy = "community", cascade = CascadeType.ALL)
    private List<Post> posts = new ArrayList<>();

    public Community() {
    }

    public Community(String name, String description, String banner) throws SQLException {
        this.name = name;
        this.id = name.replace(" ", "_");
        this.description = description;
        if (banner != null) {
            this.banner = new SerialBlob(banner.getBytes());
        }
    }

    public String toString() {
        return this.name;
    }


}
