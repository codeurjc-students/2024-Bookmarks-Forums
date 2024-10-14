package com.example.backend.service;

import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserSampleService {
    /*
    Initializes users (dummy data).
     */

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserSampleService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() throws IOException, SQLException {
        List<String> userRoles = List.of("USER");
        List<String> adminRoles = List.of("USER", "ADMIN");

        List<User> users = new ArrayList<>();
        List<String> usernames = List.of("BookReader_14", "FanBook_785", "YourReader", "AdminReader");
        List<String> emails = List.of("bookreader14@gmail.com", "fanBook@gmail.com", "reader@gmail.com", "adminReader@gmail.com");
        List<String> aliases = List.of("BookReader", "FanB", "YourReader", "adminR");
        List<String> passwords = List.of(passwordEncoder.encode("pass"), passwordEncoder.encode("pass"), passwordEncoder.encode("pass"), passwordEncoder.encode("adminpass"));
        List<List<String>> roles = List.of(userRoles, userRoles, userRoles, adminRoles);
        List<String> descriptions = List.of("I'm a reader fan that loves fantasy books", "I love books", "I'm a reader", "I'm a Bookmarks Forums admin");

        for (int i = 0; i < usernames.size(); i++) {
            User user = new User(usernames.get(i), aliases.get(i), descriptions.get(i), null, emails.get(i), passwords.get(i), roles.get(i));
            users.add(user);
        }

        userRepository.saveAll(users);

        // FOLLOWERS AND FOLLOWING

        // BookReader_14 follows FanBook_785, YourReader, and AdminReader
        users.get(0).addFollowing(users.get(1));
        users.get(1).addFollower(users.get(0));

        users.get(0).addFollowing(users.get(2));
        users.get(2).addFollower(users.get(0));

        users.get(0).addFollowing(users.get(3));
        users.get(3).addFollower(users.get(0));

        // FanBook_785 follows BookReader_14 and YourReader
        users.get(1).addFollowing(users.get(0));
        users.get(0).addFollower(users.get(1));

        users.get(1).addFollowing(users.get(2));
        users.get(2).addFollower(users.get(1));

        // YourReader follows BookReader_14 and FanBook_785
        users.get(2).addFollowing(users.get(0));
        users.get(0).addFollower(users.get(2));

        users.get(2).addFollowing(users.get(1));
        users.get(1).addFollower(users.get(2));

        // AdminReader follows BookReader_14 and FanBook_785
        users.get(3).addFollowing(users.get(0));
        users.get(0).addFollower(users.get(3));

        users.get(3).addFollowing(users.get(1));
        users.get(1).addFollower(users.get(3));

        userRepository.saveAll(users);

    }
}
