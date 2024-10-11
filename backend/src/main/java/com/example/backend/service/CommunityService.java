package com.example.backend.service;

import com.example.backend.entity.Ban;
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

    public Community getCommunityById(Long id) {
        return communityRepository.findByIdentifier(id);
    }

    public Page<Community> getCommunityByDescription(String description, Pageable pageable) {
        return communityRepository.findByDescription(description, pageable);
    }

    public Page<User> getMembers(Long identifier, Pageable pageable) {
        return communityRepository.getMembers(identifier, pageable);
    }

    public int getNumberOfUsers(Long identifier) {
        return communityRepository.getMembersList(identifier).size();
    }

    public int getNumberOfPosts(Long identifier) {
        return communityRepository.getPostsList(identifier).size();
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
    public boolean isUserMemberOfCommunity(String username, Long communityId) {
        List<User> members = communityRepository.getMembersList(communityId);
        return members.contains(userRepository.findByUsername(username));

    }

    // Given username and communityId, check if the user is an admin of the community
    public boolean isUserAdminOfCommunity(String username, Long communityId) {
        User communityAdmin = communityRepository.getAdmin(communityId);
        return communityAdmin.getUsername().equals(username);
    }

    // Given username and communityId, user joins the community
    public void joinCommunity(String username, Long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && !isUserMemberOfCommunity(username, communityId)) {
                community.addMember(user);
                communityRepository.save(community);
            }
    }

    // Given username and communityId, user leaves the community
    public void leaveCommunity(String username, Long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && isUserMemberOfCommunity(username, communityId)) {
            community.removeMember(user);
            communityRepository.save(community);
        }
    }

    // Given username and communityId, user becomes an admin of the community
    public void promoteUserToAdmin(String username, long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && !isUserAdminOfCommunity(username, communityId)) {
            community.setAdmin(user);
            communityRepository.save(community);
        }
    }

    // Given username and communityId, user is no longer an admin of the community
    public void demoteUserFromAdmin(String username, Long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && isUserAdminOfCommunity(username, communityId)) {
            community.setAdmin(null);
            communityRepository.save(community);
        }
    }

    // Get the admin of a community
    public User getAdmin(Long communityId) {
        return communityRepository.getAdmin(communityId);
    }

    // Get the moderators of a community
    public Page<User> getModerators(Long communityId, Pageable pageable) {
        return communityRepository.getModerators(communityId, pageable);
    }

    // Is user a moderator of the community
    public boolean isUserModeratorOfCommunity(String username, Long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        return user != null && community != null && community.getModerators().contains(user);
    }

    // Given username and communityId, user becomes a moderator of the community
    public void promoteUserToModerator(String username, Long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && !community.getModerators().contains(user)) {
            community.addModerator(user);
            communityRepository.save(community);
        }
    }

    // Given username and communityId, user is no longer a moderator of the community
    public void demoteUserFromModerator(String username, Long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null && community.getModerators().contains(user)) {
            community.removeModerator(user);
            communityRepository.save(community);
        }
    }

    // Ban a user from a community
    public void banUserFromCommunity(String username, Long communityId, int duration, String reason) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null) {
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
            // remove user from community
            community.removeMember(user);
            communityRepository.save(community);
        }
    }

    // Does community already exist by name
    public boolean doesCommunityExist(String name) {
        return !communityRepository.findAllByName(name).isEmpty();
    }

    // Get ban reason of a user in a community
    public String getBanReason(String username, Long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null){
            Ban ban = communityRepository.getBan(communityId, user.getUsername());
            if (ban != null) {
                return ban.getBanReason();
            }
        }
        return null;
    }

    // Get ban duration of a user in a community
    public LocalDateTime getBanDuration(String username, Long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null){
            Ban ban = communityRepository.getBan(communityId, user.getUsername());
            if (ban != null) {
                return ban.getBanUntil();
            }
        }
        return null;
    }

    // Get banned users of a community
    public Page<Ban> getBannedUsers(Long communityId, Pageable pageable) {
        return communityRepository.getBannedUsers(communityId, pageable);
    }

    // Given username and communityId, user is no longer banned from the community
    public void unbanUserFromCommunity(String username, Long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null) {
            Ban ban = communityRepository.getBan(communityId, user.getUsername());
            if (ban != null) {
                community.unbanUser(user);
                communityRepository.save(community);
            }
        }
    }

    // Given username and communityId, check if the user is banned from the community
    public boolean isUserBannedFromCommunity(String username, Long communityId) {
        User user = userRepository.findByUsername(username);
        Community community = communityRepository.findByIdentifier(communityId);
        if (user != null && community != null) {
            Ban ban = communityRepository.getBan(communityId, user.getUsername());
            
            if (ban != null) {
                // is ban over?
                if (ban.getBanUntil().isBefore(LocalDateTime.now())) {
                    // unban user
                    community.unbanUser(user);
                    communityRepository.save(community);
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    // Given username and communityId, return the ban of the user in the community
    public Ban getBan(String username, Long communityId) {
        return communityRepository.getBan(communityId, username);
    }

}
