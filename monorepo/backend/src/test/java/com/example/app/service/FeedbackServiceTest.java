package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.app.model.Feedback;
import com.example.app.repository.FeedbackRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    private String testEmail;
    private UUID testEpisodeId;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testEpisodeId = UUID.randomUUID();
    }

    @Test
    void saveFeedback_NewFeedback_ShouldCreateNew() {
        // Arrange
        when(feedbackRepository.findByUserEmailAndEpisodeId(testEmail, testEpisodeId))
                .thenReturn(Optional.empty());
        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        feedbackService.saveFeedback(testEmail, testEpisodeId, true);

        // Assert
        verify(feedbackRepository, times(1)).findByUserEmailAndEpisodeId(testEmail, testEpisodeId);
        verify(feedbackRepository, times(1)).save(any(Feedback.class));
    }

    @Test
    void saveFeedback_ExistingFeedback_ShouldUpdate() {
        // Arrange
        Feedback existingFeedback = new Feedback(testEmail, testEpisodeId, false);
        when(feedbackRepository.findByUserEmailAndEpisodeId(testEmail, testEpisodeId))
                .thenReturn(Optional.of(existingFeedback));
        when(feedbackRepository.save(any(Feedback.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        feedbackService.saveFeedback(testEmail, testEpisodeId, true);

        // Assert
        verify(feedbackRepository, times(1)).findByUserEmailAndEpisodeId(testEmail, testEpisodeId);
        verify(feedbackRepository, times(1)).save(existingFeedback);
        assertTrue(existingFeedback.getIsHelpful());
    }

    @Test
    void feedbackExists_FeedbackExists_ShouldReturnTrue() {
        // Arrange
        Feedback feedback = new Feedback(testEmail, testEpisodeId, true);
        when(feedbackRepository.findByUserEmailAndEpisodeId(testEmail, testEpisodeId))
                .thenReturn(Optional.of(feedback));

        // Act
        boolean result = feedbackService.feedbackExists(testEmail, testEpisodeId);

        // Assert
        assertTrue(result);
        verify(feedbackRepository, times(1)).findByUserEmailAndEpisodeId(testEmail, testEpisodeId);
    }

    @Test
    void feedbackExists_FeedbackDoesNotExist_ShouldReturnFalse() {
        // Arrange
        when(feedbackRepository.findByUserEmailAndEpisodeId(testEmail, testEpisodeId))
                .thenReturn(Optional.empty());

        // Act
        boolean result = feedbackService.feedbackExists(testEmail, testEpisodeId);

        // Assert
        assertFalse(result);
        verify(feedbackRepository, times(1)).findByUserEmailAndEpisodeId(testEmail, testEpisodeId);
    }
}
