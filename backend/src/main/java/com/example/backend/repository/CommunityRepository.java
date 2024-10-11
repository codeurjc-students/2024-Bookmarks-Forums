package com.example.backend.repository;

import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

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

        // Find communities by number of members
        @Query("SELECT c FROM Community c LEFT JOIN c.members m GROUP BY c ORDER BY COUNT(m) DESC")
        Page<Community> findCommunitiesOrderByMembers(Pageable pageable);

        // Find communities by name and sort by creation date
        @Query("SELECT c FROM Community c WHERE c.name LIKE %:name% ORDER BY c.fullCreationDate DESC")
        Page<Community> findByNameOrderByCreationDate(String name, Pageable pageable);

        // Find communities by name and sort by latest modified date
        @Query("SELECT c FROM Community c WHERE c.name LIKE %:name% ORDER BY c.fullLastPostDate DESC")
        Page<Community> findByNameOrderByLastPostDate(String name, Pageable pageable);

        // Find communities by name and sort by number of members
        @Query("SELECT c FROM Community c WHERE c.name LIKE %:name% GROUP BY c ORDER BY COUNT(c.members) DESC")
        Page<Community> findByNameOrderByMembers(String name, Pageable pageable);

        // Find communities by description and sort by creation date
        @Query("SELECT c FROM Community c WHERE c.description LIKE %:description% ORDER BY c.fullCreationDate DESC")
        Page<Community> findByDescriptionOrderByCreationDate(String description, Pageable pageable);

        // Find communities by description and sort by latest modified date
        @Query("SELECT c FROM Community c WHERE c.description LIKE %:description% ORDER BY c.fullLastPostDate DESC")
        Page<Community> findByDescriptionOrderByLastPostDate(String description, Pageable pageable);

        // Find communities by description and sort by number of members
        @Query("SELECT c FROM Community c WHERE c.description LIKE %:description% GROUP BY c ORDER BY COUNT(c.members) DESC")
        Page<Community> findByDescriptionOrderByMembers(String description, Pageable pageable);

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

        // Return posts count of a community
        @Query("SELECT COUNT(c.posts) FROM Community c WHERE c.identifier = :communityId")
        int getPostsCount(String communityId);

        // Get members of a community
        @Query("SELECT c.members FROM Community c WHERE c.identifier = :communityId")
        Page<User> getMembers(String communityId, Pageable pageable);

        @Query("SELECT c.members FROM Community c WHERE c.identifier = :communityId")
        List<User> getMembersList(String communityId);

        // Get admin of a community
        @Query("SELECT c.admin FROM Community c WHERE c.identifier = :communityId")
        User getAdmin(String communityId);

        // Get moderators of a community
        @Query("SELECT c.moderators FROM Community c WHERE c.identifier = :communityId")
        Page<User> getModerators(String communityId, Pageable pageable);

}
