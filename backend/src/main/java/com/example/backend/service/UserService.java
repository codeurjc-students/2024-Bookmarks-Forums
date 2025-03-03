package com.example.backend.service;

import com.example.backend.entity.*;
import com.example.backend.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.sql.Blob;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;
    private final BanRepository banRepository;

    public UserService(UserRepository userRepository, CommunityRepository communityRepository,
            ReplyRepository replyRepository, PostRepository postRepository, BanRepository banRepository) {

        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.replyRepository = replyRepository;
        this.postRepository = postRepository;
        this.banRepository = banRepository;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
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
        User user = userRepository.findByUsername(username);
        if (user != null) {
            return user.getCommunities().size();
        } else {
            return -1;
        }
    }

    public int getNumberOfAdminCommunities(String username) {
        int communitiesCount = userRepository.getNumberOfAdminCommunities(username);
        return communitiesCount;
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
        // Can't follow yourself or someone you're already following
        if (!follower.equals(following) && !follower.getFollowingList().contains(following)) {
            follower.addFollowing(following);
            following.addFollower(follower);
            userRepository.save(follower);
            userRepository.save(following);
        }
    }

    public void unfollowUser(User follower, User following) {
        // Can't unfollow someone you're not following or yourself
        if (follower.getFollowingList().contains(following) && !follower.equals(following)) {
            follower.removeFollowing(following);
            following.removeFollower(follower);
            userRepository.save(follower);
            userRepository.save(following);
        }
    }

    public boolean isUserFollowing(String follower, String following) {
        return userRepository.findByUsername(follower).getFollowingList()
                .contains(userRepository.findByUsername(following));
    }

    public boolean isUserFollower(String follower, String following) {
        return userRepository.findByUsername(following).getFollowersList()
                .contains(userRepository.findByUsername(follower));
    }

    public boolean isUsernameAvailable(String username) {
        return userRepository.findByUsername(username) == null;
    }

    public boolean isEmailAvailable(String email) {
        return userRepository.findByEmail(email) == null;
    }

    public void setUserImage(String username, Blob image) {
        User user = userRepository.findByUsername(username);
        user.setPfp(image);
        userRepository.save(user);
    }

    public void removeUserReferences(User user) {
        // Remove user from followers and following lists
        for (User follower : user.getFollowersList()) {
            follower.removeFollowing(user);
            userRepository.save(follower);
        }
        for (User following : user.getFollowingList()) {
            following.removeFollower(user);
            userRepository.save(following);
        }

        // Clear the lists to avoid further issues
        user.getFollowersList().clear();
        user.getFollowingList().clear();

        // Remove user from communities
        for (Community community : user.getCommunities()) {
            community.getMembers().remove(user);
            communityRepository.save(community);
        }
        user.getCommunities().clear();

        // clear the liked replies
        for (Reply reply : user.getLikedReplies()) {
            reply.getLikedBy().remove(user);
            replyRepository.save(reply);
        }

        for (Post post : user.getUpvotedPosts()) {
            post.getUpvotedBy().remove(user);
            postRepository.save(post);
        }

        for (Post post : user.getDownvotedPosts()) {
            post.getDownvotedBy().remove(user);
            postRepository.save(post);
        }

        // Remove user bans
        List<Ban> userBans = communityRepository.getBansByUser(user.getUsername());
        if (userBans != null && !userBans.isEmpty()) {
            banRepository.deleteAll(userBans);
        }

        // Save the user
        userRepository.save(user);
    }

    public Page<User> getMostPopularUsers(Pageable pageable) {
        return userRepository.getUsersWithMostLikedContent(pageable);
    }

    public List<Object[]> getMostPopularUsersCount(int size) {
        List<Object[]> userList = userRepository.getUsersWithMostLikedContentCount();
        if (userList != null) {
            if (size > userList.size()) {
                return userList;
            } else {
                return userList.subList(0, size);
            }
        } else {
            return Collections.emptyList();
        }
    }

    public List<Object[]> getMostBannedUsersCount(int size) {
        List<Object[]> userList = userRepository.getUsersWithMostBansCount();
        if (userList != null) {
            if (size > userList.size()) {
                return userList;
            } else {
                return userList.subList(0, size);
            }
        } else {
            return Collections.emptyList();
        }
    }

    public List<Object[]> getMostDislikedUsersCount(int size) {
        List<Object[]> userList = userRepository.getUsersWithMostDislikesCount();
        if (userList != null) {
            if (size > userList.size()) {
                return userList;
            } else {
                return userList.subList(0, size);
            }
        } else {
            return Collections.emptyList();
        }
    }

    // ADMIN ONLY: Disable a user account
    public User disableUser(String username, int duration) {
        User user = userRepository.findByUsername(username);
        if (user != null && !user.isDisabled()) {
            LocalDateTime timeNow = LocalDateTime.now();

            switch (duration) {
                case 1 -> timeNow = timeNow.plusDays(1);
                case 7 -> timeNow = timeNow.plusDays(7);
                case 14 -> timeNow = timeNow.plusDays(14);
                case 30 -> timeNow = timeNow.plusDays(30);
                case 180 -> timeNow = timeNow.plusDays(180);
                case 365 -> timeNow = timeNow.plusDays(365);
                case -1 -> timeNow = timeNow.plusYears(100); // forever
                default -> throw new IllegalArgumentException("Invalid duration: " + duration);
            }

            user.setDisabled(true, timeNow);
            userRepository.save(user);
        }
        return user;
    }

    // ADMIN ONLY: Enable a user account
    public User enableUser(String username) {
        User user = userRepository.findByUsername(username);
        if (user != null) {
            user.setDisabled(false);
            userRepository.save(user);
        }
        return user;
    }

    // Account disabled check
    public boolean isAccountDisabled(String username) {
        User user = userRepository.findByUsername(username);

        // If user is disabled, check if the disabledUntil time has passed
        if (user != null && user.isDisabled()) {
            LocalDateTime now = LocalDateTime.now();
            // if the disabledUntil time has passed, enable the account
            if (now.isAfter(user.getDisabledUntil())) {
                user.setDisabled(false);
                userRepository.save(user);
                return false;
            } else {
                return true;
            }
        }
        return false;
    }

    public Page<User> searchUsersOrderByBanCount(String query, Pageable pageable) {
        return userRepository.getUsersSortedByBanCount(query, pageable);
    }

}
