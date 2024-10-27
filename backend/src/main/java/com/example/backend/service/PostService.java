package com.example.backend.service;

import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.PostRepository;
import com.example.backend.repository.UserRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Blob;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public Page<Post> getPostsByAuthor(String author, Pageable pageable) {
        return postRepository.findByAuthorUsername(author, pageable);
    }

    public Page<Post> getPostsByCommunity(Long community, Pageable pageable) {
        return postRepository.findByCommunityIdentifier(community, pageable);
    }

    public Page<Post> getPostsByTitle(String title, Pageable pageable) {
        return postRepository.findByTitle(title, pageable);
    }

    public Page<Post> getPostsByContent(String content, Pageable pageable) {
        return postRepository.findByContent(content, pageable);
    }

    public Page<Post> getPostsByAuthor(User author, Pageable pageable) {
        return postRepository.findByAuthor(author, pageable);
    }

    public Page<Post> searchPosts(String query, Pageable pageable, String order) {
        return switch (order) {
            case "creationDate" -> postRepository.engineSearchPostsOrderByCreationDate(query, pageable);
            case "lastModifiedDate" -> postRepository.engineSearchPostsOrderByLastModifiedDate(query, pageable);
            case "replies" -> postRepository.engineSearchPostsOrderByReplies(query, pageable);
            case "likes" -> postRepository.engineSearchPostsOrderByLikes(query, pageable);
            default -> postRepository.engineSearchPosts(query, pageable);
        };
    }

    public Page<Post> searchPostsByCommunityIdentifier(Long communityIdentifier, String query, Pageable pageable,
            String order, boolean searchOnContent) {
        if (searchOnContent) {
            return switch (order) {
                case "creationDate" -> postRepository
                        .findByCommunityIdentifierAndQueryOrderByCreationDate(communityIdentifier, query, pageable);
                case "lastModifiedDate" -> postRepository
                        .findByCommunityIdentifierAndQueryOrderByLastModifiedDate(communityIdentifier, query, pageable);
                case "likes" ->
                    postRepository.findByCommunityIdentifierAndQueryOrderByLikes(communityIdentifier, query, pageable);
                case "replies" -> postRepository.findByCommunityIdentifierAndQueryOrderByReplies(communityIdentifier,
                        query, pageable);
                default -> postRepository.findByCommunityIdentifierAndQuery(communityIdentifier, query, pageable);
            };
        } else {
            return switch (order) {
                case "creationDate" ->
                    postRepository.findByCommunityIdentifierOrderByCreationDate(communityIdentifier, pageable);
                case "lastModifiedDate" ->
                    postRepository.findByCommunityIdentifierOrderByLastModifiedDate(communityIdentifier, pageable);
                case "likes" -> postRepository.findByCommunityIdentifierOrderByLikes(communityIdentifier, pageable);
                case "replies" -> postRepository.findByCommunityIdentifierOrderByReplies(communityIdentifier, pageable);
                default -> postRepository.findByCommunityIdentifier(communityIdentifier, pageable);
            };
        }
    }

    public void savePost(Post post) {
        // only saves the post if it has a title and content, community is not null and
        // the author is a member of the community
        if (post.getTitle() != null && post.getContent() != null && post.getCommunity() != null
                && post.getAuthor() != null && post.getCommunity().getMembers().contains(post.getAuthor())) {
            postRepository.save(post);
        }
    }

    /*
     * In order to be able to edit a post, it has to have the status "not edited" or
     * have been edited more than a week ago
     */
    public void editPost(Post post, String title, String content) {
        if (!post.isEdited() || post.getFullLastEditDate().plusDays(7).isBefore(post.getFullCreationDate())) {
            post.setTitle(title);
            post.setContent(content);
            post.setEdited(true);
            postRepository.save(post);
        }
    }

    public Post getPostById(Long id) {
        return postRepository.findByIdentifier(id);
    }

    public void deletePostById(Long id) {
        postRepository.deleteById(id);
    }

    public void deletePost(Post post) {
        postRepository.delete(post);
    }

    public void updatePost(Post post) {
        postRepository.save(post);
    }

    public void upvotePost(Post post, String username) {
        // remove the downvote if the user has downvoted the post
        if (post.getDownvotedBy().contains(userRepository.findByUsername(username))) {
            post.getDownvotedBy().remove(userRepository.findByUsername(username));
            post.setDownvotes(post.getDownvotes() - 1);
        }
        post.upvote(userRepository.findByUsername(username));
        postRepository.save(post);
    }

    public void downvotePost(Post post, String username) {
        // remove the upvote if the user has upvoted the post
        if (post.getUpvotedBy().contains(userRepository.findByUsername(username))) {
            post.getUpvotedBy().remove(userRepository.findByUsername(username));
            post.setUpvotes(post.getUpvotes() - 1);
        }
        post.downvote(userRepository.findByUsername(username));
        postRepository.save(post);
    }

    public void removeUpvote(Post post, String username) {
        post.getUpvotedBy().remove(userRepository.findByUsername(username));
        post.setUpvotes(post.getUpvotes() - 1);
        postRepository.save(post);
    }

    public void removeDownvote(Post post, String username) {
        post.getDownvotedBy().remove(userRepository.findByUsername(username));
        post.setDownvotes(post.getDownvotes() - 1);
        postRepository.save(post);
    }

    public void setImage(Post post, Blob image) {
        post.addImage(image);
        postRepository.save(post);
    }

    // Has the given user upvoted the given post?
    public boolean hasUserUpvotedPost(String username, Long postId) {
        return userRepository.getUpvotedPosts(username).contains(postRepository.findByIdentifier(postId));
    }

    // Has the given user downvoted the given post?
    public boolean hasUserDownvotedPost(String username, Long postId) {
        return userRepository.getDownvotedPosts(username).contains(postRepository.findByIdentifier(postId));
    }

}
