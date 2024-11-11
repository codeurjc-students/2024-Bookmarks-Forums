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
    public void init() {
        List<User> users = userRepository.findAll();
        List<Community> communities = communityRepository.findAll();
        List<String> postTitles = List.of("Welcome to Bookmarks Forums", "Bookmarks News: New Book Releases", "Bookmarks Reviews: The Best Books", "Bookmarks Events: Upcoming Events");
        List<String> postContents = List.of("Welcome to Bookmarks Forums! This is a forum for book readers to discuss their favorite books.", "Check out the new book releases in Bookmarks News!", "Read the reviews of the best books in Bookmarks Reviews.", "Join us for the upcoming events for book readers in Bookmarks Events.");

        List<Post> posts = new ArrayList<>();

        for (int i = 0; i < postTitles.size(); i++) {
            Post post = new Post(postTitles.get(i), postContents.get(i), users.get(i), communities.get(i));
            posts.add(post);
        }

        postRepository.saveAll(posts);

        // REPLIES

        List<String> replyTitles = List.of("Welcome!", "New Releases", "Best Books", "Upcoming Events");
        List<String> replyContents = List.of("Welcome to Bookmarks Forums! I'm excited to discuss books with everyone.", "I'm looking forward to the new releases in Bookmarks News!", "I can't wait to read the reviews of the best books in Bookmarks Reviews.", "I'm interested in the upcoming events for book readers in Bookmarks Events.");

        for (int i = 0; i < replyTitles.size(); i++) {
            Reply reply = new Reply(replyTitles.get(i), replyContents.get(i), users.get(i), posts.get(i));
            replyRepository.save(reply);
        }

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
