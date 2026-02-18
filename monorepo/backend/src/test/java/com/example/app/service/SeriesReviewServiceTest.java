package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.app.exception.ResourceNotFoundException;
import com.example.app.model.SeriesReview;
import com.example.app.repository.SeriesReviewRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeriesReviewServiceTest {

  @Mock
  private SeriesReviewRepository seriesReviewRepository;

  @Mock
  private WatchProgressService watchProgressService;

  private SeriesReviewService seriesReviewService;
  private final double threshold = 80.0;
  private final long editWindowHours = 24;

  @BeforeEach
  void setUp() {
    seriesReviewService = new SeriesReviewService(
        seriesReviewRepository, watchProgressService, threshold, editWindowHours);
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
  void getReviewsForSeries_ShouldReturnRecentFirst_WhenSortIsNull() {
    UUID seriesId = UUID.randomUUID();
    List<SeriesReview> mockReviews = List.of(new SeriesReview());
    when(seriesReviewRepository.findBySeriesIdAndDeletedFalseOrderByIsVerifiedDescCreatedAtDesc(seriesId))
        .thenReturn(mockReviews);

    List<SeriesReview> result = seriesReviewService.getReviewsForSeries(seriesId, null);

    assertEquals(1, result.size());
    verify(seriesReviewRepository).findBySeriesIdAndDeletedFalseOrderByIsVerifiedDescCreatedAtDesc(seriesId);
  }

  @Test
  void getReviewsForSeries_ShouldReturnRecentFirst_WhenSortIsRecent() {
    UUID seriesId = UUID.randomUUID();
    List<SeriesReview> mockReviews = List.of(new SeriesReview());
    when(seriesReviewRepository.findBySeriesIdAndDeletedFalseOrderByIsVerifiedDescCreatedAtDesc(seriesId))
        .thenReturn(mockReviews);

    List<SeriesReview> result = seriesReviewService.getReviewsForSeries(seriesId, "recent");

    assertEquals(1, result.size());
    verify(seriesReviewRepository).findBySeriesIdAndDeletedFalseOrderByIsVerifiedDescCreatedAtDesc(seriesId);
  }

  @Test
  void getReviewsForSeries_ShouldReturnOldestFirst_WhenSortIsOldest() {
    UUID seriesId = UUID.randomUUID();
    List<SeriesReview> mockReviews = List.of(new SeriesReview());
    when(seriesReviewRepository.findBySeriesIdAndDeletedFalseOrderByCreatedAtAsc(seriesId))
        .thenReturn(mockReviews);

    List<SeriesReview> result = seriesReviewService.getReviewsForSeries(seriesId, "oldest");

    assertEquals(1, result.size());
    verify(seriesReviewRepository).findBySeriesIdAndDeletedFalseOrderByCreatedAtAsc(seriesId);
  }

  @Test
  void updateReview_ShouldUpdateFields_WhenWithinEditWindow() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();
    SeriesReview existing = new SeriesReview(email, seriesId, 3, "OK", 90.0, true);
    existing.setCreatedAt(LocalDateTime.now().minusHours(1));

    when(seriesReviewRepository.findByUserEmailAndSeriesId(email, seriesId))
        .thenReturn(Optional.of(existing));
    when(seriesReviewRepository.save(any(SeriesReview.class))).thenAnswer(i -> i.getArguments()[0]);

    SeriesReview result = seriesReviewService.updateReview(email, seriesId, 5, "Updated!");

    assertEquals(5, result.getRating());
    assertEquals("Updated!", result.getComment());
    assertNotNull(result.getUpdatedAt());
    verify(seriesReviewRepository).save(existing);
  }

  @Test
  void updateReview_ShouldThrow_WhenEditWindowExpired() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();
    SeriesReview existing = new SeriesReview(email, seriesId, 3, "OK", 90.0, true);
    existing.setCreatedAt(LocalDateTime.now().minusHours(25));

    when(seriesReviewRepository.findByUserEmailAndSeriesId(email, seriesId))
        .thenReturn(Optional.of(existing));

    IllegalStateException ex = assertThrows(
        IllegalStateException.class,
        () -> seriesReviewService.updateReview(email, seriesId, 5, "Late edit"));

    assertEquals("Edit window has expired.", ex.getMessage());
    verify(seriesReviewRepository, never()).save(any());
  }

  @Test
  void updateReview_ShouldThrow_WhenReviewIsFlagged() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();
    SeriesReview existing = new SeriesReview(email, seriesId, 3, "OK", 90.0, true);
    existing.setCreatedAt(LocalDateTime.now().minusHours(1));
    existing.setFlagged(true);

    when(seriesReviewRepository.findByUserEmailAndSeriesId(email, seriesId))
        .thenReturn(Optional.of(existing));

    IllegalStateException ex = assertThrows(
        IllegalStateException.class,
        () -> seriesReviewService.updateReview(email, seriesId, 5, "Flagged edit"));

    assertEquals("Cannot edit a flagged review.", ex.getMessage());
    verify(seriesReviewRepository, never()).save(any());
  }

  @Test
  void updateReview_ShouldThrow_WhenReviewNotFound() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();

    when(seriesReviewRepository.findByUserEmailAndSeriesId(email, seriesId))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> seriesReviewService.updateReview(email, seriesId, 5, "No review"));
  }

  @Test
  void isEditable_ShouldReturnTrue_WhenWithinWindowAndNotFlagged() {
    SeriesReview review = new SeriesReview();
    review.setCreatedAt(LocalDateTime.now().minusHours(1));
    review.setFlagged(false);

    assertTrue(seriesReviewService.isEditable(review));
  }

  @Test
  void isEditable_ShouldReturnFalse_WhenWindowExpired() {
    SeriesReview review = new SeriesReview();
    review.setCreatedAt(LocalDateTime.now().minusHours(25));
    review.setFlagged(false);

    assertFalse(seriesReviewService.isEditable(review));
  }

  @Test
  void isEditable_ShouldReturnFalse_WhenFlagged() {
    SeriesReview review = new SeriesReview();
    review.setCreatedAt(LocalDateTime.now().minusHours(1));
    review.setFlagged(true);

    assertFalse(seriesReviewService.isEditable(review));
  }

  @Test
  void isEditable_ShouldReturnFalse_WhenDeleted() {
    SeriesReview review = new SeriesReview();
    review.setCreatedAt(LocalDateTime.now().minusHours(1));
    review.setFlagged(false);
    review.setDeleted(true);

    assertFalse(seriesReviewService.isEditable(review));
  }

  @Test
  void deleteReview_ShouldSoftDelete_WhenReviewExists() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();
    SeriesReview existing = new SeriesReview(email, seriesId, 4, "Nice!", 90.0, true);
    existing.setCreatedAt(LocalDateTime.now().minusHours(1));

    when(seriesReviewRepository.findByUserEmailAndSeriesId(email, seriesId))
        .thenReturn(Optional.of(existing));
    when(seriesReviewRepository.save(any(SeriesReview.class))).thenAnswer(i -> i.getArguments()[0]);

    seriesReviewService.deleteReview(email, seriesId);

    assertTrue(existing.isDeleted());
    assertNotNull(existing.getUpdatedAt());
    verify(seriesReviewRepository).save(existing);
  }

  @Test
  void deleteReview_ShouldThrow_WhenReviewNotFound() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();

    when(seriesReviewRepository.findByUserEmailAndSeriesId(email, seriesId))
        .thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> seriesReviewService.deleteReview(email, seriesId));

    verify(seriesReviewRepository, never()).save(any());
  }

  @Test
  void deleteReview_ShouldThrow_WhenAlreadyDeleted() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();
    SeriesReview existing = new SeriesReview(email, seriesId, 4, "Nice!", 90.0, true);
    existing.setCreatedAt(LocalDateTime.now().minusHours(1));
    existing.setDeleted(true);

    when(seriesReviewRepository.findByUserEmailAndSeriesId(email, seriesId))
        .thenReturn(Optional.of(existing));

    IllegalStateException ex = assertThrows(
        IllegalStateException.class,
        () -> seriesReviewService.deleteReview(email, seriesId));

    assertEquals("Review is already deleted.", ex.getMessage());
    verify(seriesReviewRepository, never()).save(any());
  }

  @Test
  void submitReview_ShouldThrow_WhenDeletedReviewExists() {
    String email = "user@example.com";
    UUID seriesId = UUID.randomUUID();
    when(seriesReviewRepository.existsByUserEmailAndSeriesId(email, seriesId)).thenReturn(true);

    assertThrows(
        IllegalStateException.class,
        () -> seriesReviewService.submitReview(email, seriesId, 5, "Re-submit after delete"));

    verify(seriesReviewRepository, never()).save(any());
  }

  @Test
  void getReviewsForSeries_ReturnsVerifiedReviewsFirst() {
    // Arrange
    UUID seriesId = UUID.randomUUID();
    String sort = "recent";
    SeriesReview verifiedReview = new SeriesReview();
    verifiedReview.setVerified(true);
    verifiedReview.setCreatedAt(LocalDateTime.now().minusDays(1));

    SeriesReview unverifiedReview = new SeriesReview();
    unverifiedReview.setVerified(false);
    unverifiedReview.setCreatedAt(LocalDateTime.now()); // Newer but unverified

    List<SeriesReview> expectedReviews = List.of(verifiedReview, unverifiedReview);

    when(seriesReviewRepository.findBySeriesIdAndDeletedFalseOrderByIsVerifiedDescCreatedAtDesc(seriesId))
        .thenReturn(expectedReviews);

    // Act
    List<SeriesReview> result = seriesReviewService.getReviewsForSeries(seriesId, sort);

    // Assert
    assertEquals(expectedReviews, result);
    verify(seriesReviewRepository).findBySeriesIdAndDeletedFalseOrderByIsVerifiedDescCreatedAtDesc(seriesId);
  }
}
