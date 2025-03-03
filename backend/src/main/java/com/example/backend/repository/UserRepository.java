package com.example.backend.repository;

import com.example.backend.entity.Community;
import com.example.backend.entity.Post;
import com.example.backend.entity.Reply;
import com.example.backend.entity.User;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {
        User findByUsername(String username);

        User findByEmail(String email);

        Page<User> findByAlias(String alias, Pageable pageable);

        @Operation(summary = "Search users by username, email, alias or description. Search Engine's default behaviour")
        @Query("SELECT u FROM User u WHERE " +
                        "u.username LIKE %:query% OR " +
                        "u.email LIKE %:query% OR " +
                        "u.alias LIKE %:query% OR " +
                        "u.description LIKE %:query%")
        Page<User> engineSearchUsers(@Param("query") String query, Pageable pageable);

        // Communities a user is a member of
        @Query("SELECT u.communities FROM User u WHERE u.username LIKE %:username%")
        Page<Community> getUserCommunities(@Param("username") String username, Pageable pageable);

        // Communities a user is an admin of
        @Query("SELECT c FROM Community c WHERE c.admin.username LIKE %:username%")
        Page<Community> findCommunitiesAdmin(String username, Pageable pageable);

        // Get the number of communities a user is a member of
        @Query("SELECT COUNT(u.communities) FROM User u WHERE u.username LIKE %:username%")
        int getNumberOfCommunities(@Param("username") String username);

        // Get the number of communities a user is an admin of
        @Query("SELECT COUNT(c) FROM Community c WHERE c.admin.username LIKE %:username%")
        int getNumberOfAdminCommunities(@Param("username") String username);

        // Get the number of posts a user has made
        @Query("SELECT COUNT(p) FROM Post p WHERE p.author.username LIKE %:username%")
        int getNumberOfPosts(@Param("username") String username);

        // Search engine default behaviour + sort by user's creation date
        @Query("SELECT u FROM User u WHERE " +
                        "u.username LIKE %:query% OR " +
                        "u.email LIKE %:query% OR " +
                        "u.alias LIKE %:query% OR " +
                        "u.description LIKE %:query% ORDER BY u.fullCreationDate DESC")
        Page<User> engineSearchUsersOrderByCreationDate(@Param("query") String query, Pageable pageable);

        // Get the number of followers a user has
        @Query("SELECT COUNT(u.followers) FROM User u WHERE u.username LIKE %:username%")
        int getNumberOfFollowers(@Param("username") String username);

        // Get the number of users a user is following
        @Query("SELECT COUNT(u.following) FROM User u WHERE u.username LIKE %:username%")
        int getNumberOfFollowing(@Param("username") String username);

        // Get the users a user is following
        @Query("SELECT u.followingList FROM User u WHERE u.username LIKE %:username%")
        Page<User> getFollowing(@Param("username") String username, Pageable pageable);

        // Get the users following a user
        @Query("SELECT u.followersList FROM User u WHERE u.username LIKE %:username%")
        Page<User> getFollowers(@Param("username") String username, Pageable pageable);

        // Get liked replies
        @Query("SELECT u.likedReplies FROM User u WHERE u.username LIKE %:username%")
        List<Reply> getLikedReplies(@Param("username") String username);

        // Get upvoted posts
        @Query("SELECT u.upvotedPosts FROM User u WHERE u.username LIKE %:username%")
        List<Post> getUpvotedPosts(@Param("username") String username);

        // Get downvoted posts
        @Query("SELECT u.downvotedPosts FROM User u WHERE u.username LIKE %:username%")
        List<Post> getDownvotedPosts(@Param("username") String username);

        // Get users with the most upvoted content
        @Query("SELECT u FROM User u JOIN u.posts p ORDER BY p.upvotes DESC")
        Page<User> getUsersWithMostLikedContent(Pageable pageable);

        // Get users with the most upvoted content and return both the username and the
        // total number of upvotes
        @Query("SELECT u.username, SUM(p.upvotes) as totalUpvotes FROM User u JOIN u.posts p GROUP BY u.username ORDER BY totalUpvotes DESC")
        List<Object[]> getUsersWithMostLikedContentCount();

        // Get users sorted by banCount and query by username
        @Query("SELECT u FROM User u WHERE u.username LIKE %:username% ORDER BY u.banCount DESC")
        Page<User> getUsersSortedByBanCount(@Param("username") String username, Pageable pageable);

        // Get users with most bans and return both the username and the total number of bans
        @Query("SELECT u.username, u.banCount FROM User u ORDER BY u.banCount DESC")
        List<Object[]> getUsersWithMostBansCount();

        // Get users with most dislikes and return both the username and the total number of dislikes
        @Query("SELECT u.username, SUM(p.downvotes) as totalDownvotes FROM User u JOIN u.posts p GROUP BY u.username ORDER BY totalDownvotes DESC")
        List<Object[]> getUsersWithMostDislikesCount();
}
