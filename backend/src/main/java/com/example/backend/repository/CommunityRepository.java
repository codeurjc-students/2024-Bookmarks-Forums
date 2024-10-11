package com.example.backend.repository;

import com.example.backend.entity.Ban;
import com.example.backend.entity.Community;
import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;

public interface CommunityRepository extends JpaRepository<Community, Long> {
        @Query("SELECT c FROM Community c WHERE c.name LIKE %:name%")
        Page<Community> findByName(String name, Pageable pageable);

        Community findByIdentifier(long communityId);

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
        int getMembersCount(long communityId);

        // Return posts count of a community
        @Query("SELECT COUNT(c.posts) FROM Community c WHERE c.identifier = :communityId")
        int getPostsCount(long communityId);

        // Get members of a community
        @Query("SELECT c.members FROM Community c WHERE c.identifier = :communityId")
        Page<User> getMembers(long communityId, Pageable pageable);

        @Query("SELECT c.members FROM Community c WHERE c.identifier = :communityId")
        List<User> getMembersList(long communityId);

        // Get Posts List of a community
        @Query("SELECT c.posts FROM Community c WHERE c.identifier = :communityId")
        List<Post> getPostsList(long communityId);

        // Get admin of a community
        @Query("SELECT c.admin FROM Community c WHERE c.identifier = :communityId")
        User getAdmin(long communityId);

        // Get moderators of a community
        @Query("SELECT c.moderators FROM Community c WHERE c.identifier = :communityId")
        Page<User> getModerators(long communityId, Pageable pageable);

        // Find all communities by name
        @Query("SELECT c FROM Community c WHERE c.name LIKE %:name%")
        List<Community> findAllByName(String name);

        //Get banned users of a community
        @Query("SELECT c.bannedUsers FROM Community c WHERE c.identifier = :communityId")
        Page<Ban> getBannedUsers(long communityId, Pageable pageable);

        // Get a specific ban of a community given the user username and community identifier
        @Query("SELECT b FROM Ban b WHERE b.community.identifier = :communityId AND b.user.username = :username")
        Ban getBan(long communityId, String username);

}
