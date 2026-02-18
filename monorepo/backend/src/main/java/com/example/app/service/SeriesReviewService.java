package com.example.app.service;

import com.example.app.exception.ResourceNotFoundException;
import com.example.app.model.SeriesReview;
import com.example.app.repository.SeriesReviewRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeriesReviewService {

  private final SeriesReviewRepository seriesReviewRepository;
  private final WatchProgressService watchProgressService;
  private final double verificationThreshold;
  private final long editWindowHours;

  public SeriesReviewService(
      SeriesReviewRepository seriesReviewRepository,
      WatchProgressService watchProgressService,
      @Value("${app.reviews.verification-threshold:80}") double verificationThreshold,
      @Value("${app.reviews.edit-window-hours:24}") long editWindowHours) {
    this.seriesReviewRepository = seriesReviewRepository;
    this.watchProgressService = watchProgressService;
    this.verificationThreshold = verificationThreshold;
    this.editWindowHours = editWindowHours;
  }

  @Transactional
  public SeriesReview submitReview(
      String userEmail, UUID seriesId, Integer rating, String comment) {
    if (seriesReviewRepository.existsByUserEmailAndSeriesId(userEmail, seriesId)) {
      throw new IllegalStateException("User has already reviewed this series.");
    }

    double progress = watchProgressService.calculateSeriesProgress(userEmail, seriesId);
    boolean isVerified = progress >= verificationThreshold;

    SeriesReview review = new SeriesReview(userEmail, seriesId, rating, comment, progress, isVerified);

    return seriesReviewRepository.save(review);
  }

  @Transactional
  public SeriesReview updateReview(
      String userEmail, UUID seriesId, Integer rating, String comment) {
    SeriesReview review = seriesReviewRepository
        .findByUserEmailAndSeriesId(userEmail, seriesId)
        .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

    if (review.isFlagged()) {
      throw new IllegalStateException("Cannot edit a flagged review.");
    }

    if (review.getCreatedAt().plusHours(editWindowHours).isBefore(LocalDateTime.now())) {
      throw new IllegalStateException("Edit window has expired.");
    }

    review.setRating(rating);
    review.setComment(comment);
    review.setUpdatedAt(LocalDateTime.now());
    return seriesReviewRepository.save(review);
  }

  public boolean isEditable(SeriesReview review) {
    return !review.isFlagged()
        && !review.isDeleted()
        && review.getCreatedAt().plusHours(editWindowHours).isAfter(LocalDateTime.now());
  }

  @Transactional
  public void deleteReview(String userEmail, UUID seriesId) {
    SeriesReview review = seriesReviewRepository
        .findByUserEmailAndSeriesId(userEmail, seriesId)
        .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

    if (review.isDeleted()) {
      throw new IllegalStateException("Review is already deleted.");
    }

    review.setDeleted(true);
    review.setUpdatedAt(LocalDateTime.now());
    seriesReviewRepository.save(review);
  }

  @Transactional(readOnly = true)
  public List<SeriesReview> getReviewsForSeries(UUID seriesId, String sort) {
    if ("oldest".equalsIgnoreCase(sort)) {
      return seriesReviewRepository.findBySeriesIdAndDeletedFalseOrderByCreatedAtAsc(seriesId);
    }
    return seriesReviewRepository.findBySeriesIdAndDeletedFalseOrderByIsVerifiedDescCreatedAtDesc(seriesId);
  }

  @Transactional(readOnly = true)
  public com.example.app.dto.SeriesRatingSummaryDto getRatingSummary(UUID seriesId) {
    return seriesReviewRepository.findRatingSummaryBySeriesId(seriesId);
  }
}
