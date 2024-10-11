package com.example.backend.service;

import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import com.example.backend.repository.CommunityRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import javax.xml.stream.events.Comment;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public CommunityService(CommunityRepository communityRepository, UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
    }

    public Community getCommunityById(String id) {
        return communityRepository.findByIdentifier(id);
    }

    public Page<Community> getCommunityByDescription(String description, Pageable pageable) {
        return communityRepository.findByDescription(description, pageable);
    }

    public Page<User> getMembers(String identifier, Pageable pageable) {
        return communityRepository.getMembers(identifier, pageable);
    }

    public int getNumberOfUsers(String identifier) {
        return communityRepository.getMembersCount(identifier);
    }

    public int getNumberOfPosts(String identifier) {
        return communityRepository.getPostsCount(identifier);
    }

    public Page<Community> engineSearchCommunities(String query, String sortCriteria, Pageable pageable) {
        return switch (sortCriteria) {
            case "creationDate" -> communityRepository.engineSearchCommunitiesOrderByCreationDate(query, pageable);
            case "members" -> communityRepository.engineSearchCommunitiesOrderByMembers(query, pageable);
            case "lastPostDate" -> communityRepository.engineSearchCommunitiesOrderByLastPostDate(query, pageable);
            default -> communityRepository.engineSearchCommunities(query, pageable);
        };
    }

    public Page<Community> getCommunities(String sortCriteria, Pageable pageable) {
        return switch (sortCriteria) {
            case "creationDate" -> communityRepository.findCommunitiesOrderByCreationDate(pageable);
            case "members" -> communityRepository.findCommunitiesOrderByMembers(pageable);
            case "lastPostDate" -> communityRepository.findCommunitiesOrderByLastPostDate(pageable);
            default -> communityRepository.findAll(pageable);
        };
    }

    public Page<Community> getCommunitiesByAdmin(String username, Pageable pageable) {
        return communityRepository.findByAdminUsername(username, pageable);
    }

    public Page<Community> getCommunitiesByName(String name, String sortCriteria, Pageable pageable) {
        return switch (sortCriteria) {
            case "creationDate" -> communityRepository.findByNameOrderByCreationDate(name, pageable);
            case "members" -> communityRepository.findByNameOrderByMembers(name, pageable);
            case "lastPostDate" -> communityRepository.findByNameOrderByLastPostDate(name, pageable);
            default -> communityRepository.findByName(name, pageable);
        };
    }

    public Page<Community> getCommunitiesByDescription(String description, String sortCriteria, Pageable pageable) {
        return switch (sortCriteria) {
            case "creationDate" -> communityRepository.findByDescriptionOrderByCreationDate(description, pageable);
            case "members" -> communityRepository.findByDescriptionOrderByMembers(description, pageable);
            case "lastPostDate" -> communityRepository.findByDescriptionOrderByLastPostDate(description, pageable);
            default -> communityRepository.findByDescription(description, pageable);
        };
    }

    public void saveCommunity(Community community) {
        communityRepository.save(community);
    }

    public void deleteCommunity(Community community) {
        if (community != null) {
            communityRepository.delete(community);
        }
    }

    // Given username and communityId, check if the user is a member of the community
    public boolean isUserMemberOfCommunity(String username, String communityId) {
        List<User> members = communityRepository.getMembersList(communityId);
        return members.contains(userRepository.findByUsername(username));

    }

    // Given username and communityId, check if the user is an admin of the community
    public boolean isUserAdminOfCommunity(String username, String communityId) {
        User communityAdmin = communityRepository.getAdmin(communityId);
        return communityAdmin.getUsername().equals(username);
    }

    // Given username and communityId, user joins the community
    public void joinCommunity(String username, String communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && !isUserMemberOfCommunity(username, communityId)) {
                community.addMember(user);
                communityRepository.save(community);
            }
    }

    // Given username and communityId, user leaves the community
    public void leaveCommunity(String username, String communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && isUserMemberOfCommunity(username, communityId)) {
            community.removeMember(user);
            communityRepository.save(community);
        }
    }

    // Given username and communityId, user becomes an admin of the community
    public void promoteUserToAdmin(String username, String communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && !isUserAdminOfCommunity(username, communityId)) {
            community.setAdmin(user);
            communityRepository.save(community);
        }
    }

    // Given username and communityId, user is no longer an admin of the community
    public void demoteUserFromAdmin(String username, String communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && isUserAdminOfCommunity(username, communityId)) {
            community.setAdmin(null);
            communityRepository.save(community);
        }
    }

    // Get the admin of a community
    public User getAdmin(String communityId) {
        return communityRepository.getAdmin(communityId);
    }

    // Get the moderators of a community
    public Page<User> getModerators(String communityId, Pageable pageable) {
        return communityRepository.getModerators(communityId, pageable);
    }

    // Given username and communityId, user becomes a moderator of the community
    public void promoteUserToModerator(String username, String communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && !community.getModerators().contains(user)) {
            community.addModerator(user);
            communityRepository.save(community);
        }
    }

    // Given username and communityId, user is no longer a moderator of the community
    public void demoteUserFromModerator(String username, String communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && community.getModerators().contains(user)) {
            community.removeModerator(user);
            communityRepository.save(community);
        }
    }

    // Ban a user from a community
    public void banUserFromCommunity(String username, String communityId, int duration, String reason) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && isUserMemberOfCommunity(username, communityId)) {
            // transform duratio into LocalDateTime format from now
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
            
            community.banUser(user, timeNow, reason);
        }
    }

}
