package com.example.backend.service;

import com.example.backend.entity.Reply;
import com.example.backend.repository.ReplyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ReplyService {

    private final ReplyRepository replyRepository;

    public ReplyService(ReplyRepository replyRepository) {
        this.replyRepository = replyRepository;
    }

    public boolean existsByPostIDAndAuthorUsername(Long postID, String authorUsername) {
        return replyRepository.existsByPostIdentifierAndAuthorUsername(postID, authorUsername);
    }

    public Reply saveReply(Reply reply) {
        if (!existsByPostIDAndAuthorUsername(reply.getPost().getIdentifier(), reply.getAuthor().getUsername())) {
            replyRepository.save(reply);
            return reply;
        } else {
            return null;
        }
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

    public Page<Reply> searchRepliesByPost(Long postId, String query, Pageable pageable) {
        return replyRepository.findByPostAndQuery(postId, query, pageable);
    }
}