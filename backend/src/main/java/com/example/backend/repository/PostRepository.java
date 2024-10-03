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

    // Search post by community name
    @Query("SELECT p FROM Post p WHERE p.community.name LIKE %:communityName%")
    Page<Post> findByCommunityName(@Param("communityName") String communityName, Pageable pageable);

    // Search post by community name and sort by latest modified date
    @Query("SELECT p FROM Post p WHERE p.community.name LIKE %:communityName% ORDER BY p.fullLastReplyDate DESC")
    Page<Post> findByCommunityNameOrderByLastModifiedDate(@Param("communityName") String communityName, Pageable pageable);

    // Search post by community name and sort by creation date
    @Query("SELECT p FROM Post p WHERE p.community.name LIKE %:communityName% ORDER BY p.fullCreationDate DESC")
    Page<Post> findByCommunityNameOrderByCreationDate(@Param("communityName") String communityName, Pageable pageable);

    // Search post by community name and sort by number of likes
    @Query("SELECT p FROM Post p WHERE p.community.name LIKE %:communityName% ORDER BY p.upvotes DESC")
    Page<Post> findByCommunityNameOrderByLikes(@Param("communityName") String communityName, Pageable pageable);

    // Search post by community name and title or content text
    @Query("SELECT p FROM Post p WHERE " +
            "p.community.name LIKE %:communityName% AND " +
            "(p.title LIKE %:query% OR " +
            "p.content LIKE %:query%)")
    Page<Post> findByCommunityNameAndQuery(@Param("communityName") String communityName, @Param("query") String query, Pageable pageable);

    // Search post by community name and title or content text and sort by latest modified date
    @Query("SELECT p FROM Post p WHERE " +
            "p.community.name LIKE %:communityName% AND " +
            "(p.title LIKE %:query% OR " +
            "p.content LIKE %:query%) ORDER BY p.fullLastReplyDate DESC")
    Page<Post> findByCommunityNameAndQueryOrderByLastModifiedDate(@Param("communityName") String communityName, @Param("query") String query, Pageable pageable);

    // Search post by community name and title or content text and sort by creation date
    @Query("SELECT p FROM Post p WHERE " +
            "p.community.name LIKE %:communityName% AND " +
            "(p.title LIKE %:query% OR " +
            "p.content LIKE %:query%) ORDER BY p.fullCreationDate DESC")
    Page<Post> findByCommunityNameAndQueryOrderByCreationDate(@Param("communityName") String communityName, @Param("query") String query, Pageable pageable);

    // Search post by community name and title or content text and sort by number of likes
    @Query("SELECT p FROM Post p WHERE " +
            "p.community.name LIKE %:communityName% AND " +
            "(p.title LIKE %:query% OR " +
            "p.content LIKE %:query%) ORDER BY p.upvotes DESC")
    Page<Post> findByCommunityNameAndQueryOrderByLikes(@Param("communityName") String communityName, @Param("query") String query, Pageable pageable);


    // Search post by community identifier
    @Query("SELECT p FROM Post p WHERE p.community.identifier LIKE %:communityIdentifier%")
    Page<Post> findByCommunityIdentifier(@Param("communityIdentifier") String communityIdentifier, Pageable pageable);

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
}
