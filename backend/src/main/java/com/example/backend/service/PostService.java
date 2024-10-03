package com.example.backend.service;

import com.example.backend.entity.Post;
import com.example.backend.entity.User;
import com.example.backend.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Blob;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Page<Post> getPostsByAuthor(String author, Pageable pageable) {
        return postRepository.findByAuthorUsername(author, pageable);
    }

    public Page<Post> getPostsByCommunity(String community, Pageable pageable) {
        return postRepository.findByCommunityIdentifier(community, pageable);
    }

    public Page<Post> getPostsByTitle(String title, Pageable pageable) {
        return postRepository.findByTitle(title, pageable);
    }

    public Page<Post> getPostsByContent(String content, Pageable pageable) {
        return postRepository.findByContent(content, pageable);
    }

    public Page<Post> getPostsByCommunityName(String communityName, Pageable pageable) {
        return postRepository.findByCommunityName(communityName, pageable);
    }

    public Page<Post> getPostsByAuthor(User author, Pageable pageable) {
        return postRepository.findByAuthor(author, pageable);
    }

    public Page<Post> searchPosts (String query, Pageable pageable, String order) {
        return switch (order) {
            case "creationDate" -> postRepository.engineSearchPostsOrderByCreationDate(query, pageable);
            case "lastModifiedDate" -> postRepository.engineSearchPostsOrderByLastModifiedDate(query, pageable);
            case "replies" -> postRepository.engineSearchPostsOrderByReplies(query, pageable);
            case "likes" -> postRepository.engineSearchPostsOrderByLikes(query, pageable);
            default -> postRepository.engineSearchPosts(query, pageable);
        };
    }

    public Page<Post> searchPostsByCommunityName (String communityName, String query, Pageable pageable, String order, boolean searchOnContent) {
        if (searchOnContent) {
            return switch (order) {
                case "creationDate" -> postRepository.findByCommunityNameAndQueryOrderByCreationDate(communityName, query, pageable);
                case "lastModifiedDate" -> postRepository.findByCommunityNameAndQueryOrderByLastModifiedDate(communityName, query, pageable);
                case "likes" -> postRepository.findByCommunityNameAndQueryOrderByLikes(communityName, query, pageable);
                default -> postRepository.findByCommunityNameAndQuery(communityName, query, pageable);
            };
        } else {
            return switch (order) {
                case "creationDate" -> postRepository.findByCommunityNameOrderByCreationDate(communityName, pageable);
                case "lastModifiedDate" -> postRepository.findByCommunityNameOrderByLastModifiedDate(communityName, pageable);
                case "likes" -> postRepository.findByCommunityNameOrderByLikes(communityName, pageable);
                default -> postRepository.findByCommunityName(communityName, pageable);
            };
        }
    }

    public void savePost(Post post) {
        postRepository.save(post);
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

    public void upvotePost(Post post) {
        post.upvote();
        postRepository.save(post);
    }

    public void downvotePost(Post post) {
        post.downvote();
        postRepository.save(post);
    }

    public void setImage(Post post, Blob image) {
        post.addImage(image);
        postRepository.save(post);
    }

}
