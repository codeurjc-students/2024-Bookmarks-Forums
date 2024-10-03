package com.example.backend.service;

import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import com.example.backend.repository.CommunityRepository;
import com.example.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public CommunityService(CommunityRepository communityRepository, UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
    }

    public Page<Community> getCommunityByName(String name, Pageable pageable) {
        return communityRepository.findByName(name, pageable);
    }

    public Community getCommunityById(String id) {
        return communityRepository.findByIdentifier(id);
    }

    public Page<Community> getCommunityByDescription(String description, Pageable pageable) {
        return communityRepository.findByDescription(description, pageable);
    }

    public Page<User> getMembers(String name, Pageable pageable) {
        return communityRepository.getMembers(name, pageable);
    }

    public int getNumberOfUsers(String name) {
        return communityRepository.getMembersCount(name);
    }

    public int getNumberOfPosts(String name) {
        return communityRepository.getPostsCount(name);
    }

    public Page<Community> engineSearchCommunities(String query, String sortCriteria, Pageable pageable) {
        return switch (sortCriteria) {
            case "creationDate" -> communityRepository.engineSearchCommunitiesOrderByCreationDate(query, pageable);
            case "members" -> communityRepository.engineSearchCommunitiesOrderByMembers(query, pageable);
            case "posts" -> communityRepository.engineSearchCommunitiesOrderByLastPostDate(query, pageable);
            default -> communityRepository.engineSearchCommunities(query, pageable);
        };
    }

    public Page<Community> getCommunities(boolean sortByCreationDate, Pageable pageable) {
        if (sortByCreationDate) {
            return communityRepository.findCommunitiesOrderByCreationDate(pageable);
        } else {
            return communityRepository.findCommunitiesOrderByLastPostDate(pageable);
        }
    }

    public Page<Community> getCommunitiesByAdmin(String username, Pageable pageable) {
        return communityRepository.findByAdminUsername(username, pageable);
    }

    public Page<Community> getCommunitiesByName(String name, String sortCriteria, Pageable pageable) {
        return switch (sortCriteria) {
            case "name" -> communityRepository.findByNameOrderByLastPostDate(name, pageable);
            case "posts" -> communityRepository.findByDescriptionOrderByLastPostDate(name, pageable);
            default -> communityRepository.findByName(name, pageable);
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

}
