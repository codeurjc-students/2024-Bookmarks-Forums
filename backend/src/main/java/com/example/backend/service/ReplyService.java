package com.example.backend.service;

import com.example.backend.entity.Reply;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.ReplyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;

    public ReplyService(ReplyRepository replyRepository, UserRepository userRepository) {
        this.replyRepository = replyRepository;
        this.userRepository = userRepository;
    }

    public boolean existsByPostIDAndAuthorUsername(Long postID, String authorUsername) {
        return replyRepository.existsByPostIdentifierAndAuthorUsername(postID, authorUsername);
    }

    public Reply saveReply(Reply reply) {
        return replyRepository.save(reply);
    }

    public void deleteReply(Reply reply) {
        if (reply != null) {
            replyRepository.delete(reply);
        }
    }

    public Reply getReplyById(Long id) {
        return replyRepository.findByIdentifier(id);
    }

    public Page<Reply> getRepliesByTitle(String title, Pageable pageable) {
        return replyRepository.findByTitle(title, pageable);
    }

    public Page<Reply> getReplyByContent(String content, Pageable pageable) {
        return replyRepository.findByContent(content, pageable);
    }

    public Page<Reply> getRepliesByAuthor(String author, Pageable pageable) {
        return replyRepository.findByAuthorUsername(author, pageable);
    }

    public Page<Reply> getRepliesByPost(Long postId, Pageable pageable, String order) {
        return switch (order) {
            case "creationDate" -> replyRepository.findByPostOrderByCreationDate(postId, pageable);
            case "rating" -> replyRepository.findByPostOrderByRating(postId, pageable);
            default -> replyRepository.findByPostIdentifier(postId, pageable);
        };
    }

    public Page<Reply> searchReplies(String query, Pageable pageable) {
        return replyRepository.engineSearchReplies(query, pageable);
    }

    public Page<Reply> searchRepliesByPost(Long postId, String query, String mode, Pageable pageable) {
        return switch (mode) {
            case "title" -> replyRepository.findByPostAndTitle(postId, query, pageable);
            case "content" -> replyRepository.findByPostAndContent(postId, query, pageable);
            case "author" -> replyRepository.findByPostAndAuthor(postId, query, pageable);
            default -> replyRepository.findByPostAndQuery(postId, query, pageable);
        };
    }

    public void likeReply(Reply reply, String username) {
        reply.setLikes(reply.getLikes() + 1);
        reply.addLikedBy(userRepository.findByUsername(username));
        replyRepository.save(reply);
    }

    public void unlikeReply(Reply reply, String username) {
        reply.setLikes(reply.getLikes() - 1);
        reply.removeLikedBy(userRepository.findByUsername(username));
        replyRepository.save(reply);
    }

    // Has the given user liked the given reply?
    public boolean hasUserLikedReply(String username, Long replyId) {
        return userRepository.getLikedReplies(username).contains(replyRepository.findByIdentifier(replyId));
    }
}
