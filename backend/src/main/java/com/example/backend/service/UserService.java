package com.example.backend.service;

import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Blob;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {

        this.userRepository = userRepository;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Page<User> getUserByEmail(String email, Pageable pageable) {
        return userRepository.findByEmail(email, pageable);
    }

    public Page<User> getUserByAlias(String alias, Pageable pageable) {
        return userRepository.findByAlias(alias, pageable);
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }

    public void deleteUser(User user) {
        if (user != null) {
            userRepository.delete(user);
        }
    }

    public Page<User> searchUsers(String query, Pageable pageable) {
        return userRepository.engineSearchUsers(query, pageable);
    }

    public Page<User> searchUsersOrderByCreationDate(String query, Pageable pageable) {
        return userRepository.engineSearchUsersOrderByCreationDate(query, pageable);
    }

    public int getNumberOfCommunities(String username) {
        return userRepository.getNumberOfCommunities(username);
    }

    public int getNumberOfAdminCommunities(String username) {
        return userRepository.getNumberOfAdminCommunities(username);
    }

    public int getNumberOfPosts(String username) {
        return userRepository.getNumberOfPosts(username);
    }

    // Get the communities a user is a member of
    public Page<Community> findCommunities(String username, Pageable pageable) {
        return userRepository.getUserCommunities(username, pageable);
    }

    // Get the communities a user is an admin of
    public Page<Community> findAdminCommunities(String username, Pageable pageable) {
        return userRepository.findCommunitiesAdmin(username, pageable);
    }

    public int getNumberOfFollowers(String username) {
        return userRepository.getNumberOfFollowers(username);
    }

    public int getNumberOfFollowing(String username) {
        return userRepository.getNumberOfFollowing(username);
    }

    public Page<User> getFollowing(String username, Pageable pageable) {
        return userRepository.getFollowing(username, pageable);
    }

    public Page<User> getFollowers(String username, Pageable pageable) {
        return userRepository.getFollowers(username, pageable);
    }

    public void followUser(User follower, User following) {
        follower.addFollowing(following);
        following.addFollower(follower);
        userRepository.save(follower);
        userRepository.save(following);
    }

    public void unfollowUser(User follower, User following) {
        follower.removeFollowing(following);
        following.removeFollower(follower);
        userRepository.save(follower);
        userRepository.save(following);
    }

    public boolean isUserFollowing(String follower, String following) {
        return userRepository.findByUsername(follower).getFollowingList().contains(userRepository.findByUsername(following));
    }

    public boolean isUserFollower(String follower, String following) {
        return userRepository.findByUsername(following).getFollowersList().contains(userRepository.findByUsername(follower));
    }

    public boolean isUsernameAvailable(String username){
        return userRepository.findByUsername(username) == null;
    }

    public void setUserImage(String username, Blob image) {
        User user = userRepository.findByUsername(username);
        user.setPfp(image);
        userRepository.save(user);
    }
}
