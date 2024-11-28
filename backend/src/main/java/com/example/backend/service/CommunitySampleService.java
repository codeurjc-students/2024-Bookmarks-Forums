package com.example.backend.service;

import com.example.backend.repository.CommunityRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@DependsOn("userSampleService")
public class CommunitySampleService {

    private final CommunityRepository communityRepository;

    private final UserRepository userRepository;

    public CommunitySampleService(CommunityRepository communityRepository, UserRepository userRepository) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        List<Community> communities = new ArrayList<>();
        List<String> communityNames = List.of("Bookmarks Forums", "Bookmarks News", "Bookmarks Reviews", "Bookmarks Events");
        List<String> descriptions = List.of("A forum for book readers", "News about books", "Reviews of books", "Events for book readers");

        for (int i = 0; i < communityNames.size()-1; i++) {
            Community community = new Community(communityNames.get(i), descriptions.get(i), null, userRepository.findByUsername("AdminReader"));
            communities.add(community);
        }

        // FanBook_785 is admin of the last community
        Community lastCommunity = new Community(communityNames.getLast(), descriptions.getLast(), null, userRepository.findByUsername("FanBook_785"));
        communities.add(lastCommunity);

        communityRepository.saveAll(communities);

        // MEMBERS
        // BookReader_14 is a member of all communities
        User user = userRepository.findByUsername("BookReader_14");
        for (Community community : communities) {
            community.addMember(user);
        }

        // FanBook_785 is a member of Bookmarks Forums and Bookmarks News
        user = userRepository.findByUsername("FanBook_785");
        for (int i = 0; i < 2; i++) {
            communities.get(i).addMember(user);
        }

        // YourReader is a member of bookmarks forums
        user = userRepository.findByUsername("YourReader");
        communities.get(0).addMember(user);

        communityRepository.saveAll(communities);
    }
}
