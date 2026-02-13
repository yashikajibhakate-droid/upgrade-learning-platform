package com.example.app.controller;

import com.example.app.dto.SeriesReviewRequest;
import com.example.app.dto.SeriesReviewResponse;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.model.Series;
import com.example.app.model.SeriesReview;
import com.example.app.model.User;
import com.example.app.service.SeriesReviewService;
import com.example.app.service.SeriesService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

  private static final Logger log = LoggerFactory.getLogger(SeriesController.class);

  @Autowired
  private SeriesService seriesService;
  @Autowired
  private SeriesReviewService seriesReviewService;

  @GetMapping("/recommendations")
  public ResponseEntity<?> getRecommendations(@RequestParam String email) {
    if (email == null || email.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(seriesService.getRecommendations(email));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Series> getSeriesById(@PathVariable UUID id) {
    try {
      return ResponseEntity.ok(seriesService.getSeriesById(id));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/{id}/episodes")
  public ResponseEntity<java.util.List<com.example.app.model.Episode>> getEpisodes(
      @PathVariable UUID id) {
    return ResponseEntity.ok(seriesService.getEpisodesForSeries(id));
  }

  @PostMapping("/{seriesId}/reviews")
  public ResponseEntity<?> submitReview(
      HttpServletRequest request,
      @PathVariable UUID seriesId,
      @Valid @RequestBody SeriesReviewRequest reviewRequest) {

    User user = (User) request.getAttribute("user");
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Authentication required"));
    }

    try {
      SeriesReview review = seriesReviewService.submitReview(
          user.getEmail(), seriesId, reviewRequest.rating(), reviewRequest.comment());
      return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(review, user.getEmail()));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Unexpected error in submitReview for user {} and series {}",
          user.getEmail(), seriesId, e);
      throw e;
    }
  }

  @PutMapping("/{seriesId}/reviews")
  public ResponseEntity<?> updateReview(
      HttpServletRequest request,
      @PathVariable UUID seriesId,
      @Valid @RequestBody SeriesReviewRequest reviewRequest) {

    User user = (User) request.getAttribute("user");
    if (user == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(Map.of("error", "Authentication required"));
    }

    try {
      SeriesReview review = seriesReviewService.updateReview(
          user.getEmail(), seriesId, reviewRequest.rating(), reviewRequest.comment());
      return ResponseEntity.ok(mapToResponse(review, user.getEmail()));
    } catch (ResourceNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      log.error("Unexpected error in updateReview for user {} and series {}",
          user.getEmail(), seriesId, e);
      throw e;
    }
  }

  @GetMapping("/{seriesId}/reviews")
  public ResponseEntity<List<SeriesReviewResponse>> getReviews(
      HttpServletRequest request,
      @PathVariable UUID seriesId) {
    User user = (User) request.getAttribute("user");
    String requestingEmail = user != null ? user.getEmail() : null;

    List<SeriesReview> reviews = seriesReviewService.getReviewsForSeries(seriesId);
    List<SeriesReviewResponse> response = reviews.stream()
        .map(r -> mapToResponse(r, requestingEmail))
        .collect(Collectors.toList());
    return ResponseEntity.ok(response);
  }

  private SeriesReviewResponse mapToResponse(SeriesReview review, String requestingEmail) {
    boolean editable = requestingEmail != null
        && requestingEmail.equals(review.getUserEmail())
        && seriesReviewService.isEditable(review);

    return new SeriesReviewResponse(
        review.getId(),
        review.getMaskedUserEmail(),
        review.getSeriesId(),
        review.getRating(),
        review.getComment(),
        review.getProgressPercentage(),
        review.isVerified(),
        review.getCreatedAt(),
        review.getUpdatedAt(),
        review.isFlagged(),
        editable);
  }
}
