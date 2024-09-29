package com.example.backend.repository;

import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
    User findByEmail(String email);
    User findByAlias(String alias);

    @Operation(summary = "Search users by username, email, alias or description. Search Engine's default behaviour")
    @Query("SELECT u FROM User u WHERE " +
            "u.username LIKE %:query% OR " +
            "u.email LIKE %:query% OR " +
            "u.alias LIKE %:query% OR " +
            "u.description LIKE %:query%")
    Page<User> engineSearchUsers(@Param("query") String query, Pageable pageable);

    // Communities a user is a member of
    @Query("SELECT u.communities FROM User u WHERE u.username LIKE %:username%")
    Page<Community> findCommunities(@Param("username") String username, Pageable pageable);

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
}
