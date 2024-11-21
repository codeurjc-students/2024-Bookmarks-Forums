package com.example.backend.repository;

import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {
        Post findByIdentifier(Long postId);

        // Search post by title
        Page<Post> findByTitle(String title, Pageable pageable);

        // Search post by content
        Page<Post> findByContent(String content, Pageable pageable);

        // Search post by author
        Page<Post> findByAuthor(User author, Pageable pageable);

        // Search post by author username
        Page<Post> findByAuthorUsername(String authorUsername, Pageable pageable);

        // Search post by community identifier
        @Query("SELECT p FROM Post p WHERE p.community.identifier = :communityIdentifier")
        Page<Post> findByCommunityIdentifier(@Param("communityIdentifier") Long communityIdentifier,
                        Pageable pageable);

        // Search post by community identifier and sort by latest modified date
        @Query("SELECT p FROM Post p WHERE p.community.identifier = :communityIdentifier ORDER BY p.fullLastReplyDate DESC")
        Page<Post> findByCommunityIdentifierOrderByLastModifiedDate(
                        @Param("communityIdentifier") Long communityIdentifier,
                        Pageable pageable);

        // Search post by community identifier and sort by creation date
        @Query("SELECT p FROM Post p WHERE p.community.identifier = :communityIdentifier ORDER BY p.fullCreationDate DESC")
        Page<Post> findByCommunityIdentifierOrderByCreationDate(
                        @Param("communityIdentifier") Long communityIdentifier,
                        Pageable pageable);

        // Search post by community identifier and sort by number of likes
        @Query("SELECT p FROM Post p WHERE p.community.identifier = :communityIdentifier ORDER BY p.upvotes DESC")
        Page<Post> findByCommunityIdentifierOrderByLikes(@Param("communityIdentifier") Long communityIdentifier,
                        Pageable pageable);

        // Search post by community identifier and sort by number of replies
        @Query("SELECT p FROM Post p LEFT JOIN p.replyList r WHERE p.community.identifier = :communityIdentifier GROUP BY p ORDER BY COUNT(r) DESC")
        Page<Post> findByCommunityIdentifierOrderByReplies(@Param("communityIdentifier") Long communityIdentifier,
                        Pageable pageable);

        // Search post by community identifier and title or content text
        @Query("SELECT p FROM Post p WHERE " +
                        "p.community.identifier = :communityIdentifier AND " +
                        "(p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%)")
        Page<Post> findByCommunityIdentifierAndQuery(@Param("communityIdentifier") Long communityIdentifier,
                        @Param("query") String query, Pageable pageable);

        // Search post by community identifier and title or content text and sort by
        // modified date
        @Query("SELECT p FROM Post p WHERE " +
                        "p.community.identifier = :communityIdentifier AND " +
                        "(p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%) ORDER BY p.fullLastReplyDate DESC")
        Page<Post> findByCommunityIdentifierAndQueryOrderByLastModifiedDate(
                        @Param("communityIdentifier") Long communityIdentifier,
                        @Param("query") String query, Pageable pageable);

        // Search post by community identifier and title or content text and sort by
        // creation date
        @Query("SELECT p FROM Post p WHERE " +
                        "p.community.identifier = :communityIdentifier AND " +
                        "(p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%) ORDER BY p.fullCreationDate DESC")
        Page<Post> findByCommunityIdentifierAndQueryOrderByCreationDate(
                        @Param("communityIdentifier") Long communityIdentifier,
                        @Param("query") String query, Pageable pageable);

        // Search post by community identifier and title or content text and sort by
        // number of likes
        @Query("SELECT p FROM Post p WHERE " +
                        "p.community.identifier = :communityIdentifier AND " +
                        "(p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%) ORDER BY p.upvotes DESC")
        Page<Post> findByCommunityIdentifierAndQueryOrderByLikes(
                        @Param("communityIdentifier") Long communityIdentifier, @Param("query") String query,
                        Pageable pageable);

        // Search post by community identifier and title or content text and sort by
        // number of replies
        @Query("SELECT p FROM Post p LEFT JOIN p.replyList r WHERE " +
                        "p.community.identifier = :communityIdentifier AND " +
                        "(p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%) GROUP BY p ORDER BY COUNT(r) DESC")
        Page<Post> findByCommunityIdentifierAndQueryOrderByReplies(
                        @Param("communityIdentifier") Long communityIdentifier,
                        @Param("query") String query, Pageable pageable);

        // Search post by title or content
        @Operation(summary = "Search posts by title or content. Search Engine's default behaviour")
        @Query("SELECT p FROM Post p WHERE " +
                        "p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%")
        Page<Post> engineSearchPosts(@Param("query") String query, Pageable pageable);

        // Search engine default behaviour + sort by latest modified date
        @Query("SELECT p FROM Post p WHERE " +
                        "p.title LIKE %:query% OR " +
                        "p.content LIKE %:query% ORDER BY p.fullLastReplyDate DESC")
        Page<Post> engineSearchPostsOrderByLastModifiedDate(@Param("query") String query, Pageable pageable);

        // Search engine default behaviour + sort by creation date
        @Query("SELECT p FROM Post p WHERE " +
                        "p.title LIKE %:query% OR " +
                        "p.content LIKE %:query% ORDER BY p.fullCreationDate DESC")
        Page<Post> engineSearchPostsOrderByCreationDate(@Param("query") String query, Pageable pageable);

        // Search engine default behaviour + sort by number of replies
        @Query("SELECT p FROM Post p LEFT JOIN p.replyList r GROUP BY p ORDER BY COUNT(r) DESC")
        Page<Post> engineSearchPostsOrderByReplies(String query, Pageable pageable);

        // Search engine default behaviour + sort by number of likes
        @Query("SELECT p FROM Post p WHERE " +
                        "p.title LIKE %:query% OR " +
                        "p.content LIKE %:query% ORDER BY p.upvotes DESC")
        Page<Post> engineSearchPostsOrderByLikes(@Param("query") String query, Pageable pageable);

        // Return the most liked posts of the most followed users a user follows
        @Query("SELECT p FROM Post p WHERE p.author IN " +
                        "(SELECT u.followingList FROM User u WHERE u.username = :username) " +
                        "ORDER BY p.upvotes DESC")
        Page<Post> getMostLikedPostsOfMostFollowedUsers(@Param("username") String username, Pageable pageable);

        // Return the most liked posts of the communities a user follows
        @Query("SELECT p FROM Post p WHERE p.community IN " +
                        "(SELECT u.communities FROM User u WHERE u.username = :username) " +
                        "ORDER BY p.upvotes DESC")
        Page<Post> getMostLikedPostsOfFollowedCommunities(@Param("username") String username, Pageable pageable);

        // Return the most recent posts of the communities a user follows
        @Query("SELECT p FROM Post p WHERE p.community IN " +
                        "(SELECT u.communities FROM User u WHERE u.username = :username) " +
                        "ORDER BY p.fullCreationDate DESC")
        Page<Post> getMostRecentPostsOfFollowedCommunities(@Param("username") String username, Pageable pageable);

        // Return the most liked posts of the most followed communities
        @Query("SELECT p FROM Post p WHERE p.community IN " +
                        "(SELECT c FROM Community c ORDER BY SIZE(c.members) DESC) " +
                        "ORDER BY p.upvotes DESC")
        Page<Post> getMostLikedPostsOfMostFollowedCommunities(Pageable pageable);

        // Return the most recent posts of the most followed communities
        @Query("SELECT p FROM Post p WHERE p.community IN " +
                        "(SELECT c FROM Community c ORDER BY SIZE(c.members) DESC) " +
                        "ORDER BY p.fullCreationDate DESC")
        Page<Post> getMostRecentPostsOfMostFollowedCommunities(Pageable pageable);

        // Return the most liked posts of the most followed users
        @Query("SELECT p FROM Post p WHERE p.author IN " +
                        "(SELECT u FROM User u ORDER BY SIZE(u.followersList) DESC) " +
                        "ORDER BY p.upvotes DESC")
        Page<Post> getMostLikedPostsOfMostFollowedUsersGeneral(Pageable pageable);

        // Get posts of a specified user by username
        @Query("SELECT p FROM Post p WHERE p.author.username = :username")
        Page<Post> getPostsOfUser(@Param("username") String username, Pageable pageable);

        // Get posts of a specified user by username and sort by recent date
        @Query("SELECT p FROM Post p WHERE p.author.username = :username ORDER BY p.fullCreationDate DESC")
        Page<Post> getPostsOfUserOrderByCreationDate(@Param("username") String username, Pageable pageable);

        // Get posts of a specified user by username and sort by last modified date
        @Query("SELECT p FROM Post p WHERE p.author.username = :username ORDER BY p.fullLastReplyDate DESC")
        Page<Post> getPostsOfUserOrderByLastModifiedDate(@Param("username") String username, Pageable pageable);

        // Get posts of a specified user by username and sort by number of likes
        @Query("SELECT p FROM Post p WHERE p.author.username = :username ORDER BY p.upvotes DESC")
        Page<Post> getPostsOfUserOrderByLikes(@Param("username") String username, Pageable pageable);

        // Get posts of a specified user by username and sort by number of replies
        @Query("SELECT p FROM Post p LEFT JOIN p.replyList r WHERE p.author.username = :username GROUP BY p ORDER BY COUNT(r) DESC")
        Page<Post> getPostsOfUserOrderByReplies(@Param("username") String username, Pageable pageable);

        // Get posts of a specified user by username and search by title or content
        @Query("SELECT p FROM Post p WHERE p.author.username = :username AND " +
                        "(p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%)")
        Page<Post> getPostsOfUserAndQuery(@Param("username") String username, @Param("query") String query, Pageable pageable);

        // Get posts of a specified user by username and search by title or content and sort by recent date
        @Query("SELECT p FROM Post p WHERE p.author.username = :username AND " +
                        "(p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%) ORDER BY p.fullCreationDate DESC")
        Page<Post> getPostsOfUserAndQueryOrderByCreationDate(@Param("username") String username, @Param("query") String query,
                        Pageable pageable);

        // Get posts of a specified user by username and search by title or content and sort by last modified date
        @Query("SELECT p FROM Post p WHERE p.author.username = :username AND " +
                        "(p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%) ORDER BY p.fullLastReplyDate DESC")
        Page<Post> getPostsOfUserAndQueryOrderByLastModifiedDate(@Param("username") String username, @Param("query") String query,
                        Pageable pageable);

        // Get posts of a specified user by username and search by title or content and sort by number of likes
        @Query("SELECT p FROM Post p WHERE p.author.username = :username AND " +
                        "(p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%) ORDER BY p.upvotes DESC")
        Page<Post> getPostsOfUserAndQueryOrderByLikes(@Param("username") String username, @Param("query") String query, Pageable pageable);

        // Get posts of a specified user by username and search by title or content and sort by number of replies
        @Query("SELECT p FROM Post p LEFT JOIN p.replyList r WHERE p.author.username = :username AND " +
                        "(p.title LIKE %:query% OR " +
                        "p.content LIKE %:query%) GROUP BY p ORDER BY COUNT(r) DESC")
        Page<Post> getPostsOfUserAndQueryOrderByReplies(@Param("username") String username, @Param("query") String query, Pageable pageable);
        
}
