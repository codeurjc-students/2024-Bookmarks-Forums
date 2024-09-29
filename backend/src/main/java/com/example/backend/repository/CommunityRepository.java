package com.example.backend.repository;

import com.example.backend.entity.Community;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CommunityRepository extends JpaRepository<Community, String> {
    Page<Community> findByName(String name, Pageable pageable);

    Community findByIdentifier(String communityId);

    Page<Community> findByAdminUsername(String username, Pageable pageable);

    Page<Community> findByDescription(String description, Pageable pageable);

    @Operation(summary = "Search communities by name or description. Search Engine's default behaviour")
    @Query("SELECT c FROM Community c WHERE " +
            "c.name LIKE %:query% OR " +
            "c.description LIKE %:query%")
    Page<Community> engineSearchCommunities(String query, Pageable pageable);

    // Find latest created communities
    @Query("SELECT c FROM Community c ORDER BY c.fullCreationDate DESC")
    Page<Community> findCommunitiesOrderByCreationDate(Pageable pageable);

    // FInd latest modified communities
    @Query("SELECT c FROM Community c ORDER BY c.fullLastPostDate DESC")
    Page<Community> findCommunitiesOrderByLastPostDate(Pageable pageable);

    // Find communities by name and sort by latest modified date
    @Query("SELECT c FROM Community c WHERE c.name LIKE %:name% ORDER BY c.fullLastPostDate DESC")
    Page<Community> findByNameOrderByLastPostDate(String name, Pageable pageable);

    // Find communities by description and sort by latest modified date
    @Query("SELECT c FROM Community c WHERE c.description LIKE %:description% ORDER BY c.fullLastPostDate DESC")
    Page<Community> findByDescriptionOrderByLastPostDate(String description, Pageable pageable);

    // Search engine default behaviour + sort by latest modified date
    @Query("SELECT c FROM Community c WHERE " +
            "c.name LIKE %:query% OR " +
            "c.description LIKE %:query% ORDER BY c.fullLastPostDate DESC")
    Page<Community> engineSearchCommunitiesOrderByLastPostDate(String query, Pageable pageable);

    // Search engine default behaviour + sort by creation date
    @Query("SELECT c FROM Community c WHERE " +
            "c.name LIKE %:query% OR " +
            "c.description LIKE %:query% ORDER BY c.fullCreationDate DESC")
    Page<Community> engineSearchCommunitiesOrderByCreationDate(String query, Pageable pageable);

    // Search engine default behaviour + sort by number of members
    @Query("SELECT c FROM Community c LEFT JOIN c.members m GROUP BY c ORDER BY COUNT(m) DESC")
    Page<Community> engineSearchCommunitiesOrderByMembers(String query, Pageable pageable);

    // Return members count of a community
    @Query("SELECT COUNT(c.members) FROM Community c WHERE c.identifier = :communityId")
    int getMembersCount(String communityId);



}
