package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.app.model.Episode;
import com.example.app.model.Feedback;
import com.example.app.model.Series;
import com.example.app.model.User;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.FeedbackRepository;
import com.example.app.repository.UserRepository;
import java.time.LocalDateTime;
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

  @Mock
  private EpisodeRepository episodeRepository;

  @Mock
  private UserRepository userRepository;

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
  void saveFeedback_NewFeedback_ShouldCreateNewAndAdjustWeight() {
    String category = "Java";

    // Mock Episode and Series
    Episode episode = mock(Episode.class);
    Series series = mock(Series.class);
    when(episodeRepository.findById(testEpisodeId)).thenReturn(Optional.of(episode));
    when(episode.getSeries()).thenReturn(series);
    when(series.getCategory()).thenReturn(category);

    // Mock User
    User user = new User(testEmail);
    when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));

    // Mock Feedback lookup
    when(feedbackRepository.findByUserEmailAndEpisodeId(testEmail, testEpisodeId))
        .thenReturn(Optional.empty());
    when(feedbackRepository.save(any(Feedback.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act
    feedbackService.saveFeedback(testEmail, testEpisodeId, true);

    // Assert
    verify(feedbackRepository, times(1)).save(any(Feedback.class));
    verify(userRepository, times(1)).save(user);
    assertEquals(5, user.getInterestWeights().get(category));
  }

  @Test
  void saveFeedback_ExistingFeedback_ShouldUpdateAndAdjustWeight() {
    String category = "Java";

    // Mock Episode and Series
    Episode episode = mock(Episode.class);
    Series series = mock(Series.class);
    when(episodeRepository.findById(testEpisodeId)).thenReturn(Optional.of(episode));
    when(episode.getSeries()).thenReturn(series);
    when(series.getCategory()).thenReturn(category);

    // Mock User
    User user = new User(testEmail);
    // User already has +5 weight for this category
    user.getInterestWeights().put(category, 5);
    when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(user));

    // Existing helpful feedback
    Feedback existingFeedback = new Feedback(testEmail, testEpisodeId, true);
    when(feedbackRepository.findByUserEmailAndEpisodeId(testEmail, testEpisodeId))
        .thenReturn(Optional.of(existingFeedback));

    when(feedbackRepository.save(any(Feedback.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    // Act: Change to Not Helpful
    feedbackService.saveFeedback(testEmail, testEpisodeId, false);

    // Assert
    verify(feedbackRepository, times(1)).save(existingFeedback);
    assertFalse(existingFeedback.getIsHelpful());

    // Verify weight update: +5 -> -5 means -10 change. New weight should be -5.
    verify(userRepository, times(1)).save(user);
    assertEquals(-5, user.getInterestWeights().get(category));
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
