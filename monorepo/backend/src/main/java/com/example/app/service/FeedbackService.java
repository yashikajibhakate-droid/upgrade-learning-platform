package com.example.app.service;

import com.example.app.model.Feedback;
import com.example.app.repository.FeedbackRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

  private final FeedbackRepository feedbackRepository;

  public FeedbackService(FeedbackRepository feedbackRepository) {
    this.feedbackRepository = feedbackRepository;
  }

  @Transactional
  public void saveFeedback(String userEmail, UUID episodeId, Boolean isHelpful) {
    Optional<Feedback> existing =
        feedbackRepository.findByUserEmailAndEpisodeId(userEmail, episodeId);

    if (existing.isPresent()) {
      // Update existing feedback
      Feedback feedback = existing.get();
      feedback.setIsHelpful(isHelpful);
      feedback.setUpdatedAt(LocalDateTime.now());
      feedbackRepository.save(feedback);
    } else {
      // Create new feedback
      Feedback feedback = new Feedback(userEmail, episodeId, isHelpful);
      feedbackRepository.save(feedback);
    }
  }

  public boolean feedbackExists(String userEmail, UUID episodeId) {
    return feedbackRepository.findByUserEmailAndEpisodeId(userEmail, episodeId).isPresent();
  }
}
