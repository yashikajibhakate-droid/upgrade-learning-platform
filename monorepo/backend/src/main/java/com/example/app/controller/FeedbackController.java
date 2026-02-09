package com.example.app.controller;

import com.example.app.model.User;
import com.example.app.service.FeedbackService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

  private final FeedbackService feedbackService;

  public FeedbackController(FeedbackService feedbackService) {
    this.feedbackService = feedbackService;
  }

  @PostMapping("/save")
  public ResponseEntity<Map<String, String>> saveFeedback(
      HttpServletRequest request, @Valid @RequestBody SaveFeedbackRequest feedbackRequest) {
    User user = (User) request.getAttribute("user");
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Authentication required"));
    }

    feedbackService.saveFeedback(
        user.getEmail(), feedbackRequest.getEpisodeId(), feedbackRequest.getIsHelpful());
    return ResponseEntity.ok(Map.of("message", "Feedback saved successfully"));
  }

  @PostMapping("/exists")
  public ResponseEntity<Map<String, Boolean>> feedbackExists(
      HttpServletRequest request, @Valid @RequestBody FeedbackExistsRequest feedbackRequest) {
    User user = (User) request.getAttribute("user");
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("exists", false));
    }

    boolean exists =
        feedbackService.feedbackExists(user.getEmail(), feedbackRequest.getEpisodeId());
    return ResponseEntity.ok(Map.of("exists", exists));
  }

  static class SaveFeedbackRequest {
    @NotNull(message = "Episode ID is required")
    private UUID episodeId;

    @NotNull(message = "Feedback value (isHelpful) is required")
    private Boolean isHelpful;

    public UUID getEpisodeId() {
      return episodeId;
    }

    public void setEpisodeId(UUID episodeId) {
      this.episodeId = episodeId;
    }

    public Boolean getIsHelpful() {
      return isHelpful;
    }

    public void setIsHelpful(Boolean isHelpful) {
      this.isHelpful = isHelpful;
    }
  }

  static class FeedbackExistsRequest {
    @NotNull(message = "Episode ID is required")
    private UUID episodeId;

    public UUID getEpisodeId() {
      return episodeId;
    }

    public void setEpisodeId(UUID episodeId) {
      this.episodeId = episodeId;
    }
  }
}
