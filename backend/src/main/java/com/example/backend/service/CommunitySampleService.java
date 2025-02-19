package com.example.backend.service;

import com.example.backend.entity.Ban;
import com.example.backend.repository.BanRepository;
import com.example.backend.repository.CommunityRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import com.example.backend.entity.Community;
import com.example.backend.entity.User;
import com.example.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@DependsOn("userSampleService")
public class CommunitySampleService {

    private final CommunityRepository communityRepository;

    private final UserRepository userRepository;
    private final BanRepository banRepository;

    public CommunitySampleService(CommunityRepository communityRepository, UserRepository userRepository, BanRepository banRepository) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.banRepository = banRepository;
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
        Community lastCommunity = new Community(communityNames.get(communityNames.size() -1), descriptions.get(descriptions.size() -1), null, userRepository.findByUsername("FanBook_785"));
        communities.add(lastCommunity);

        communityRepository.saveAll(communities);

        // MEMBERS
        // BookReader_14 is a member of all communities
        User user = userRepository.findByUsername("BookReader_14");
        for (Community community : communities) {
            community.addMember(user);
        }

        // FanBook_785 is a member of Bookmarks News
        user = userRepository.findByUsername("FanBook_785");
        for (int i = 1; i < 2; i++) {
            communities.get(i).addMember(user);
        }

        // YourReader is a member of bookmarks forums
        user = userRepository.findByUsername("YourReader");
        communities.get(0).addMember(user);

        // AdminReader joins last community
        user = userRepository.findByUsername("AdminReader");
        lastCommunity.addMember(user);

        // "BadDude" is banned from the first community and second community
        user = userRepository.findByUsername("ZBadDude");
        LocalDateTime now = LocalDateTime.now();
        user.addBanCount();
        communities.get(0).banUser(user, now.plusDays(7), "because you are a bad dude");
        user.addBanCount();
        communities.get(1).banUser(user, now.plusDays(14), "because you are a bad dude");

        userRepository.save(user);

        communityRepository.saveAll(communities);
    }
}
