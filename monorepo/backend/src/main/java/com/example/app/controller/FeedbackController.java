package com.example.app.controller;

import com.example.app.service.FeedbackService;
import java.util.Map;
import java.util.UUID;
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
    public ResponseEntity<Map<String, String>> saveFeedback(@RequestBody SaveFeedbackRequest request) {
        feedbackService.saveFeedback(request.getEmail(), request.getEpisodeId(), request.getIsHelpful());
        return ResponseEntity.ok(Map.of("message", "Feedback saved successfully"));
    }

    @GetMapping("/exists")
    public ResponseEntity<Map<String, Boolean>> feedbackExists(
            @RequestParam String email, @RequestParam UUID episodeId) {
        boolean exists = feedbackService.feedbackExists(email, episodeId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    static class SaveFeedbackRequest {
        private String email;
        private UUID episodeId;
        private Boolean isHelpful;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

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
}
