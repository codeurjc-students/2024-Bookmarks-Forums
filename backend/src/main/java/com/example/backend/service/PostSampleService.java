package com.example.backend.service;

import com.example.backend.entity.Post;
import com.example.backend.entity.Reply;
import com.example.backend.repository.CommunityRepository;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.ReplyRepository;
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
import java.util.ArrayList;
import java.util.List;

@Service
@DependsOn("communitySampleService")
public class PostSampleService {

    private final CommunityRepository communityRepository;

    private final UserRepository userRepository;

    private final PostRepository postRepository;

    private final ReplyRepository replyRepository;

    public PostSampleService(CommunityRepository communityRepository, UserRepository userRepository, PostRepository postRepository, ReplyRepository replyRepository) {
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.replyRepository = replyRepository;
    }

    @PostConstruct
    public void init() throws IOException, SQLException {
        List<User> users = userRepository.findAll();
        List<Community> communities = communityRepository.findAll();
        List<String> postTitles = List.of("Welcome to Bookmarks Forums", "Bookmarks News: New Book Releases", "Bookmarks Reviews: The Best Books", "Bookmarks Events: Upcoming Events");
        List<String> postContents = List.of("Welcome to Bookmarks Forums! This is a forum for book readers to discuss their favorite books.", "Check out the new book releases in Bookmarks News!", "Read the reviews of the best books in Bookmarks Reviews.", "Join us for the upcoming events for book readers in Bookmarks Events.");
        List<String> postImages = List.of("static/assets/cmbg1.png", "static/assets/cmbg2.png", "static/assets/cmbg3.png", "static/assets/cmbg4.png");

        List<Post> posts = new ArrayList<>();

        for (int i = 0; i < postTitles.size(); i++) {
            Post post = new Post(postTitles.get(i), postContents.get(i), users.get(i), communities.get(i));
            try {
                // Load and set the image for each post
                ClassPathResource resource = new ClassPathResource(postImages.get(i));
                if (resource.exists()) {
                    Blob image = new javax.sql.rowset.serial.SerialBlob(resource.getInputStream().readAllBytes());
                    post.addImage(image);
                } else {
                    System.out.println("Warning: Image file not found: " + postImages.get(i));
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not load image for post " + i + ": " + e.getMessage());
            }
            posts.add(post);
        }

        // Another post by AdminReader without image
        Post postWithoutImage = new Post("Forum's rules apply to all communities!", "Remember to follow the rules of the forum in all communities. If you see a violation, please report it to the admin.", users.get(3), communities.get(0));
        posts.add(postWithoutImage);

        postRepository.saveAll(posts);

        // REPLIES

        List<String> replyTitles = List.of("Welcome!", "New Releases", "Best Books", "Lol dude");
        List<String> replyContents = List.of("Welcome to Bookmarks Forums! I'm excited to discuss books with everyone.", "I'm looking forward to the new releases in Bookmarks News!", "I can't wait to read the reviews of the best books in Bookmarks Reviews.", "I love this omg!!!!");

        String bannedUserReplyTitle = "I'm a bad dude";
        String bannedUserReplyContent = "This is trash, dude";

        for (int i = 0; i < replyTitles.size(); i++) {
            // +1 comment to the post
            posts.get(i).setComments(posts.get(i).getComments() + 1);
            Reply reply = new Reply(replyTitles.get(i), replyContents.get(i), users.get(i), posts.get(i));
            replyRepository.save(reply);
        }

        // Banned user posts reply on post 1
        Post postWithId1 = posts.get(0);
        Reply bannedUserReply = new Reply(bannedUserReplyTitle, bannedUserReplyContent, users.get(4), postWithId1);
        replyRepository.save(bannedUserReply);
        

        // Everybody upvotes the second post
        Post post = posts.get(1);
        for (User user : users) {
            post.upvote(user);
        }

        // The first post is upvoted by the first user and downvoted by the second user
        post = posts.get(0);
        post.upvote(users.get(0));
        post.downvote(users.get(1));

        // The third post is upvoted by 2 users and downvoted by 1 user
        post = posts.get(2);
        post.upvote(users.get(0));
        post.upvote(users.get(1));
        post.downvote(users.get(2));

        // The fourth post is upvoted by 3 users
        post = posts.get(3);
        for (int i = 0; i < 3; i++) {
            post.upvote(users.get(i));
        }

        postRepository.saveAll(posts);
    }
}
