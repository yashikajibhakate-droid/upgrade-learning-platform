package com.example.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.app.model.EpisodeComment;
import com.example.app.repository.EpisodeCommentRepository;
import com.example.app.service.EmailService;
import com.example.app.service.EpisodeCommentService;
import com.example.app.service.ModerationService;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Placed in root package due to test discovery issues in subpackages
@ExtendWith(MockitoExtension.class)
public class EpisodeCommentServiceTest {

    @Mock
    private EpisodeCommentRepository commentRepository;

    @Mock
    private ModerationService moderationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EpisodeCommentService commentService;

    private final UUID episodeId = UUID.randomUUID();
    private final String userEmail = "user@example.com";

    @Test
    void addComment_CleanContent_ShouldBeApproved() {
        String content = "Great episode!";
        when(moderationService.isClean(content)).thenReturn(true);
        when(commentRepository.save(any(EpisodeComment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EpisodeComment result = commentService.addComment(episodeId, userEmail, content);

        assertEquals(EpisodeComment.Status.APPROVED, result.getStatus());
        verify(emailService, never()).sendCommentFlaggedNotification(anyString(), anyString());
        verify(emailService, never())
                .sendAdminFlaggedNotification(anyString(), anyString(), anyString());
    }

    @Test
    void addComment_DirtyContent_ShouldBeFlagged() {
        String content = "This is spam";
        when(moderationService.isClean(content)).thenReturn(false);
        when(commentRepository.save(any(EpisodeComment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        EpisodeComment result = commentService.addComment(episodeId, userEmail, content);

        assertEquals(EpisodeComment.Status.FLAGGED, result.getStatus());
        verify(emailService).sendCommentFlaggedNotification(userEmail, content);
        verify(emailService).sendAdminFlaggedNotification(any(), eq(userEmail), eq(content));
    }

    @Test
    void getApprovedComments_ShouldReturnOnlyApproved() {
        EpisodeComment c1 = new EpisodeComment(episodeId, userEmail, "Nice", EpisodeComment.Status.APPROVED);
        EpisodeComment c2 = new EpisodeComment(episodeId, "other@example.com", "Good", EpisodeComment.Status.APPROVED);

        when(commentRepository.findByEpisodeIdAndStatusOrderByCreatedAtDesc(
                episodeId, EpisodeComment.Status.APPROVED))
                .thenReturn(Arrays.asList(c1, c2));

        List<EpisodeComment> results = commentService.getApprovedComments(episodeId);

        assertEquals(2, results.size());
        verify(commentRepository)
                .findByEpisodeIdAndStatusOrderByCreatedAtDesc(episodeId, EpisodeComment.Status.APPROVED);
    }
}
