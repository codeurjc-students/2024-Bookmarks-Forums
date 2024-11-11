package com.example.backend.service;

import com.example.backend.entity.Community;
import com.example.backend.entity.Message;
import com.example.backend.entity.Reply;
import com.example.backend.entity.User;
import com.example.backend.repository.CommunityRepository;
import com.example.backend.repository.MessageRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.ReplyRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Blob;
import java.util.Collections;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final MessageRepository messageRepository;
    private final ReplyRepository replyRepository;
    private final PostRepository postRepository;

    public UserService(UserRepository userRepository, CommunityRepository communityRepository,
            MessageRepository messageRepository, ReplyRepository replyRepository, PostRepository postRepository) {

        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.messageRepository = messageRepository;
        this.replyRepository = replyRepository;
        this.postRepository = postRepository;
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

        // Remove user from sent messages
        for (Message message : user.getSentMessages()) {
            message.setSender(null);
            messageRepository.save(message);
        }

        // Clear the list to avoid further issues
        user.getSentMessages().clear();

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
    
}
