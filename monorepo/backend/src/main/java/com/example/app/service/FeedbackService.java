package com.example.app.service;

import com.example.app.model.Feedback;
import com.example.app.model.Episode;

import com.example.app.model.Series;
import com.example.app.model.User;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.FeedbackRepository;
import com.example.app.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

  private final FeedbackRepository feedbackRepository;
  private final EpisodeRepository episodeRepository;
  private final UserRepository userRepository;

  public FeedbackService(
      FeedbackRepository feedbackRepository,
      EpisodeRepository episodeRepository,
      UserRepository userRepository) {
    this.feedbackRepository = feedbackRepository;
    this.episodeRepository = episodeRepository;
    this.userRepository = userRepository;
  }

  @Transactional
  public void saveFeedback(String userEmail, UUID episodeId, Boolean isHelpful) {
    Optional<Feedback> existing = feedbackRepository.findByUserEmailAndEpisodeId(userEmail, episodeId);

    Episode episode = episodeRepository
        .findById(episodeId)
        .orElseThrow(() -> new RuntimeException("Episode not found"));
    Series series = episode.getSeries();
    String category = series.getCategory();

    User user = userRepository
        .findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("User not found"));

    // Calculate weight adjustment
    int weightChange = 0;
    if (isHelpful) {
      weightChange = 5;
    } else {
      weightChange = -5;
    }

    if (existing.isPresent()) {
      Feedback feedback = existing.get();

      // If feedback value changed, adjust weight
      if (!feedback.getIsHelpful().equals(isHelpful)) {
        // remove old effect
        int oldWeight = feedback.getIsHelpful() ? 5 : -5;
        // if old was helpful (+5), we subtract 5. if old was not helpful (-5), we
        // should have added 5?
        // Wait, simpler:
        // New weight - Old weight
        // If changed from Helpful (+5) to Not Helpful (-5): change is -10
        // If changed from Not Helpful (-5) to Helpful (+5): change is +10

        int change = weightChange - oldWeight;

        feedback.setIsHelpful(isHelpful);
        feedback.setUpdatedAt(LocalDateTime.now());
        feedbackRepository.save(feedback);

        updateUserInterestWeight(user, category, change);
      }
    } else {
      // Create new feedback
      Feedback feedback = new Feedback(userEmail, episodeId, isHelpful);
      feedbackRepository.save(feedback);

      updateUserInterestWeight(user, category, weightChange);
    }
  }

  private void updateUserInterestWeight(User user, String category, int change) {
    java.util.Map<String, Integer> weights = user.getInterestWeights();
    weights.put(category, weights.getOrDefault(category, 0) + change);
    userRepository.save(user);
  }

  public boolean feedbackExists(String userEmail, UUID episodeId) {
    return feedbackRepository.findByUserEmailAndEpisodeId(userEmail, episodeId).isPresent();
  }
}
