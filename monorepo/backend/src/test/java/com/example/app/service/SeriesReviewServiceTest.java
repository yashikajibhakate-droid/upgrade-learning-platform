package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.app.model.SeriesReview;
import com.example.app.repository.SeriesReviewRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeriesReviewServiceTest {

  @Mock private SeriesReviewRepository seriesReviewRepository;

  @Mock private WatchProgressService watchProgressService;

  private SeriesReviewService seriesReviewService;
  private final double threshold = 80.0;

  @BeforeEach
  void setUp() {
    seriesReviewService =
        new SeriesReviewService(seriesReviewRepository, watchProgressService, threshold);
  }

  @Test
  void submitReview_ShouldSaveVerifiedReview_WhenProgressAboveThreshold() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();
    when(seriesReviewRepository.existsByUserEmailAndSeriesId(email, seriesId)).thenReturn(false);
    when(watchProgressService.calculateSeriesProgress(email, seriesId)).thenReturn(85.0);
    when(seriesReviewRepository.save(any(SeriesReview.class))).thenAnswer(i -> i.getArguments()[0]);

    SeriesReview result = seriesReviewService.submitReview(email, seriesId, 5, "Great!");

    assertNotNull(result);
    assertTrue(result.isVerified());
    assertEquals(85.0, result.getProgressPercentage());
    verify(seriesReviewRepository).save(any(SeriesReview.class));
  }

  @Test
  void submitReview_ShouldSaveUnverifiedReview_WhenProgressBelowThreshold() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();
    when(seriesReviewRepository.existsByUserEmailAndSeriesId(email, seriesId)).thenReturn(false);
    when(watchProgressService.calculateSeriesProgress(email, seriesId)).thenReturn(50.0);
    when(seriesReviewRepository.save(any(SeriesReview.class))).thenAnswer(i -> i.getArguments()[0]);

    SeriesReview result = seriesReviewService.submitReview(email, seriesId, 4, "Good.");

    assertNotNull(result);
    assertFalse(result.isVerified());
    assertEquals(50.0, result.getProgressPercentage());
    verify(seriesReviewRepository).save(any(SeriesReview.class));
  }

  @Test
  void submitReview_ShouldThrowException_WhenReviewAlreadyExists() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();
    when(seriesReviewRepository.existsByUserEmailAndSeriesId(email, seriesId)).thenReturn(true);

    assertThrows(
        IllegalStateException.class,
        () -> seriesReviewService.submitReview(email, seriesId, 5, "Repeat"));

    verify(seriesReviewRepository, never()).save(any());
  }

  @Test
  void getReviewsForSeries_ShouldReturnOrderedReviews() {
    UUID seriesId = UUID.randomUUID();
    List<SeriesReview> mockReviews = List.of(new SeriesReview());
    when(seriesReviewRepository.findBySeriesIdOrderByCreatedAtDesc(seriesId))
        .thenReturn(mockReviews);

    List<SeriesReview> result = seriesReviewService.getReviewsForSeries(seriesId);

    assertEquals(1, result.size());
    verify(seriesReviewRepository).findBySeriesIdOrderByCreatedAtDesc(seriesId);
  }
}
