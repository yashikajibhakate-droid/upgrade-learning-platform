package com.example.app.controller;

import com.example.app.dto.MCQResponse;
import com.example.app.model.User;
import com.example.app.service.MCQService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mcq")
public class MCQController {

    private final MCQService mcqService;

    public MCQController(MCQService mcqService) {
        this.mcqService = mcqService;
    }

    /**
     * Get MCQ for a specific episode.
     *
     * @param episodeId The episode ID
     * @param request   HTTP request to get authenticated user
     * @return MCQ response if exists, 404 otherwise
     */
    @GetMapping("/{episodeId}")
    public ResponseEntity<?> getMCQByEpisodeId(
            @PathVariable UUID episodeId, HttpServletRequest request) {
        // Verify user is authenticated
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        Optional<MCQResponse> mcqResponse = mcqService.getMCQByEpisodeId(episodeId);

        if (mcqResponse.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "No MCQ found for this episode"));
        }

        return ResponseEntity.ok(mcqResponse.get());
    }

    /**
     * Validate the answer to an MCQ.
     *
     * @param validateRequest Request containing MCQ ID and selected option ID
     * @param request         HTTP request to get authenticated user
     * @return Validation result with isCorrect flag and refresher URL if incorrect
     */
    @PostMapping("/validate")
    public ResponseEntity<?> validateAnswer(
            @Valid @RequestBody ValidateAnswerRequest validateRequest, HttpServletRequest request) {
        // Verify user is authenticated
        User user = (User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required"));
        }

        try {
            boolean isCorrect = mcqService.validateAnswer(
                    validateRequest.getMcqId(), validateRequest.getSelectedOptionId());

            ValidateAnswerResponse response = new ValidateAnswerResponse();
            response.setCorrect(isCorrect);

            // If incorrect, include refresher video URL
            if (!isCorrect) {
                Optional<String> refresherUrl = mcqService.getRefresherVideoUrl(validateRequest.getMcqId());
                refresherUrl.ifPresent(response::setRefresherVideoUrl);
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Request/Response DTOs
    static class ValidateAnswerRequest {
        @NotNull(message = "MCQ ID is required")
        private UUID mcqId;

        @NotNull(message = "Selected option ID is required")
        private UUID selectedOptionId;

        public UUID getMcqId() {
            return mcqId;
        }

        public void setMcqId(UUID mcqId) {
            this.mcqId = mcqId;
        }

        public UUID getSelectedOptionId() {
            return selectedOptionId;
        }

        public void setSelectedOptionId(UUID selectedOptionId) {
            this.selectedOptionId = selectedOptionId;
        }
    }

    static class ValidateAnswerResponse {
        private boolean isCorrect;
        private String refresherVideoUrl;

        public boolean isCorrect() {
            return isCorrect;
        }

        public void setCorrect(boolean correct) {
            isCorrect = correct;
        }

        public String getRefresherVideoUrl() {
            return refresherVideoUrl;
        }

        public void setRefresherVideoUrl(String refresherVideoUrl) {
            this.refresherVideoUrl = refresherVideoUrl;
        }
    }
}
