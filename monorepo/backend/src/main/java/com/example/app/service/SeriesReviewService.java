package com.example.app.service;

import com.example.app.model.SeriesReview;
import com.example.app.repository.SeriesReviewRepository;
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

  public SeriesReviewService(
      SeriesReviewRepository seriesReviewRepository,
      WatchProgressService watchProgressService,
      @Value("${app.reviews.verification-threshold:80}") double verificationThreshold) {
    this.seriesReviewRepository = seriesReviewRepository;
    this.watchProgressService = watchProgressService;
    this.verificationThreshold = verificationThreshold;
  }

  @Transactional
  public SeriesReview submitReview(
      String userEmail, UUID seriesId, Integer rating, String comment) {
    if (seriesReviewRepository.existsByUserEmailAndSeriesId(userEmail, seriesId)) {
      throw new IllegalStateException("User has already reviewed this series.");
    }

    double progress = watchProgressService.calculateSeriesProgress(userEmail, seriesId);
    boolean isVerified = progress >= verificationThreshold;

    SeriesReview review =
        new SeriesReview(userEmail, seriesId, rating, comment, progress, isVerified);

    return seriesReviewRepository.save(review);
  }

  @Transactional(readOnly = true)
  public List<SeriesReview> getReviewsForSeries(UUID seriesId) {
    return seriesReviewRepository.findBySeriesIdOrderByCreatedAtDesc(seriesId);
  }
}
