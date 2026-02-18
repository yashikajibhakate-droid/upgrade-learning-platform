package com.example.app.service;

import com.example.app.model.EpisodeComment;
import com.example.app.repository.EpisodeCommentRepository;

import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EpisodeCommentService {

    @Autowired
    private EpisodeCommentRepository commentRepository;

    @Autowired
    private ModerationService moderationService;

    @Autowired
    private EmailService emailService;

    @Value("${app.admin.email:admin@example.com}")
    private String adminEmail;

    @Transactional
    public EpisodeComment addComment(UUID episodeId, String userEmail, String content) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (content.length() > 1000) {
            throw new IllegalArgumentException("Content cannot exceed 1000 characters");
        }

        boolean isClean = moderationService.isClean(content);
        EpisodeComment.Status status = isClean ? EpisodeComment.Status.APPROVED : EpisodeComment.Status.FLAGGED;

        EpisodeComment comment = new EpisodeComment(episodeId, userEmail, content, status);
        EpisodeComment savedComment = commentRepository.save(comment);

        if (savedComment.getStatus() == EpisodeComment.Status.FLAGGED) {
            emailService.sendCommentFlaggedNotification(userEmail, content);
            emailService.sendAdminFlaggedNotification(adminEmail, userEmail, content);
        }

        return savedComment;
    }

    public List<EpisodeComment> getApprovedComments(UUID episodeId) {
        return commentRepository.findByEpisodeIdAndStatusOrderByCreatedAtDesc(
                episodeId, EpisodeComment.Status.APPROVED);
    }
}
