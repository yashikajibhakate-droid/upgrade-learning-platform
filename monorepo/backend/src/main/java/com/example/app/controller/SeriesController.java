package com.example.app.controller;

import com.example.app.dto.SeriesReviewRequest;
import com.example.app.dto.SeriesReviewResponse;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

  @Autowired private SeriesService seriesService;
  @Autowired private SeriesReviewService seriesReviewService;

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
      SeriesReview review =
          seriesReviewService.submitReview(
              user.getEmail(), seriesId, reviewRequest.rating(), reviewRequest.comment());
      return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(review));
    } catch (IllegalStateException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(Map.of("error", "An error occurred while submitting the review"));
    }
  }

  @GetMapping("/{seriesId}/reviews")
  public ResponseEntity<List<SeriesReviewResponse>> getReviews(@PathVariable UUID seriesId) {
    List<SeriesReview> reviews = seriesReviewService.getReviewsForSeries(seriesId);
    List<SeriesReviewResponse> response =
        reviews.stream().map(this::mapToResponse).collect(Collectors.toList());
    return ResponseEntity.ok(response);
  }

  private SeriesReviewResponse mapToResponse(SeriesReview review) {
    return new SeriesReviewResponse(
        review.getId(),
        review.getUserEmail(),
        review.getSeriesId(),
        review.getRating(),
        review.getComment(),
        review.getProgressPercentage(),
        review.isVerified(),
        review.getCreatedAt());
  }
}
