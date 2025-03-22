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
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
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
    public void init() throws IOException, SQLException {
        List<Community> communities = new ArrayList<>();
        List<String> communityNames = List.of("Bookmarks Forums", "Bookmarks News", "Bookmarks Reviews", "Bookmarks Events");
        List<String> descriptions = List.of("A forum for book readers", "News about books", "Reviews of books", "Events for book readers");

        // Create first community with banner
        Community firstCommunity = new Community(communityNames.get(0), descriptions.get(0), null, userRepository.findByUsername("AdminReader"));
        try {
            ClassPathResource resource = new ClassPathResource("static/assets/cusbanner1.PNG");
            if (resource.exists()) {
                Blob banner = new javax.sql.rowset.serial.SerialBlob(resource.getInputStream().readAllBytes());
                firstCommunity.setBanner(banner);
            } else {
                System.out.println("Warning: Banner image file not found: static/assets/cusbanner1.PNG");
            }
        } catch (Exception e) {
            System.out.println("Warning: Could not load banner image: " + e.getMessage());
        }
        communities.add(firstCommunity);

        // Create remaining communities without banners
        for (int i = 1; i < communityNames.size()-1; i++) {
            Community community = new Community(communityNames.get(i), descriptions.get(i), null, userRepository.findByUsername("AdminReader"));
            if (i == 2) { // Bookmarks Reviews
                try {
                    ClassPathResource resource = new ClassPathResource("static/assets/communityBackground.jpg");
                    if (resource.exists()) {
                        Blob banner = new javax.sql.rowset.serial.SerialBlob(resource.getInputStream().readAllBytes());
                        community.setBanner(banner);
                    } else {
                        System.out.println("Warning: Banner image file not found: static/assets/communityBackground.jpg");
                    }
                } catch (Exception e) {
                    System.out.println("Warning: Could not load banner image: " + e.getMessage());
                }
            }
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
