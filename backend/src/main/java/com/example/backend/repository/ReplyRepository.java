package com.example.backend.repository;

import com.example.backend.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    Reply findByIdentifier(Long replyId);

    // Find replies with a given username
    Page<Reply> findByAuthorUsername(String username, Pageable pageable);

    // Find replies with a given post
    Page<Reply> findByPostIdentifier(Long postId, Pageable pageable);

    // Find replies with a given content
    Page<Reply> findByContent(String content, Pageable pageable);

    // Find replies with a given title
    Page<Reply> findByTitle(String title, Pageable pageable);

    // Search replies by title or content
    @Query("SELECT r FROM Reply r WHERE " +
            "r.content LIKE %:query% OR " +
            "r.title LIKE %:query%")
    Page<Reply> engineSearchReplies(String query, Pageable pageable);

    // Find replies of a post and sort by latest creation date
    @Query("SELECT r FROM Reply r WHERE r.post.identifier = :postId ORDER BY r.fullCreationDate DESC")
    Page<Reply> findByPostOrderByCreationDate(Long postId, Pageable pageable);

    // Find replies of a post and sort by latest rating
    @Query("SELECT r FROM Reply r WHERE r.post.identifier = :postId ORDER BY r.likes DESC")
    Page<Reply> findByPostOrderByRating(Long postId, Pageable pageable);

    //Search replies in a post by title or content
    @Query("SELECT r FROM Reply r WHERE " +
            "r.post.identifier = :postId AND " +
            "(r.content LIKE %:query% OR " +
            "r.title LIKE %:query%)")
    Page<Reply> findByPostAndQuery(Long postId, String query, Pageable pageable);

    // Check if a reply exists by post identifier and author username
    boolean existsByPostIdentifierAndAuthorUsername(Long postID, String authorUsername);
}
