package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.app.dto.ContinueWatchingResponse;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.model.Episode;
import com.example.app.model.Series;
import com.example.app.model.WatchHistory;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.SeriesRepository;
import com.example.app.repository.WatchHistoryRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WatchProgressServiceTest {

  @Mock private WatchHistoryRepository watchHistoryRepository;
  @Mock private EpisodeRepository episodeRepository;
  @Mock private SeriesRepository seriesRepository;

  @InjectMocks private WatchProgressService watchProgressService;

  @Test
  void testGetContinueWatching_WithIncompleteEpisode_ReturnsData() {
    String email = "test@example.com";
    UUID seriesId = UUID.randomUUID();
    UUID episodeId = UUID.randomUUID();

    Series series = new Series("Test Series", "Description", "Tech", "thumb.jpg");
    Episode episode = new Episode(series, "Episode 1", "video.mp4", 600, 1);

    WatchHistory watchHistory = new WatchHistory(email, seriesId, episodeId, 120, false);
    watchHistory.setLastWatchedAt(LocalDateTime.now());

    when(watchHistoryRepository.findTop1ByUserEmailAndIsCompletedFalseOrderByLastWatchedAtDesc(
            email))
        .thenReturn(Optional.of(watchHistory));
    when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode));

    Optional<ContinueWatchingResponse> result = watchProgressService.getContinueWatching(email);

    assertTrue(result.isPresent());
    assertEquals("Test Series", result.get().getSeriesTitle());
    assertEquals("Episode 1", result.get().getEpisodeTitle());
    assertEquals(120, result.get().getProgressSeconds());
  }

  @Test
  void testGetContinueWatching_NoIncompleteEpisode_ReturnsEmpty() {
    String email = "test@example.com";

    when(watchHistoryRepository.findTop1ByUserEmailAndIsCompletedFalseOrderByLastWatchedAtDesc(
            email))
        .thenReturn(Optional.empty());

    Optional<ContinueWatchingResponse> result = watchProgressService.getContinueWatching(email);

    assertFalse(result.isPresent());
  }

  @Test
  void testSaveProgress_NewProgress_CreatesRecord() {
    String email = "test@example.com";
    UUID episodeId = UUID.randomUUID();
    Integer progressSeconds = 120;

    Series series = new Series("Test Series", "Description", "Tech", "thumb.jpg");
    Episode episode = new Episode(series, "Episode 1", "video.mp4", 600, 1);

    when(watchHistoryRepository.findByUserEmailAndEpisodeId(email, episodeId))
        .thenReturn(Optional.empty());
    when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode));

    watchProgressService.saveProgress(email, episodeId, progressSeconds);

    verify(watchHistoryRepository, times(1)).save(any(WatchHistory.class));
  }

  @Test
  void testSaveProgress_ExistingProgress_UpdatesRecord() {
    String email = "test@example.com";
    UUID episodeId = UUID.randomUUID();
    UUID seriesId = UUID.randomUUID();
    Integer progressSeconds = 240;

    Series series = new Series("Test Series", "Description", "Tech", "thumb.jpg");
    Episode episode = new Episode(series, "Episode 1", "video.mp4", 600, 1);
    WatchHistory existingHistory = new WatchHistory(email, seriesId, episodeId, 120, false);

    when(watchHistoryRepository.findByUserEmailAndEpisodeId(email, episodeId))
        .thenReturn(Optional.of(existingHistory));
    when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode));

    watchProgressService.saveProgress(email, episodeId, progressSeconds);

    assertEquals(240, existingHistory.getProgressSeconds());
    verify(watchHistoryRepository, times(1)).save(existingHistory);
  }

  @Test
  void testMarkCompleted_ExistingProgress_MarksAsComplete() {
    String email = "test@example.com";
    UUID episodeId = UUID.randomUUID();
    UUID seriesId = UUID.randomUUID();

    WatchHistory existingHistory = new WatchHistory(email, seriesId, episodeId, 580, false);

    when(watchHistoryRepository.findByUserEmailAndEpisodeId(email, episodeId))
        .thenReturn(Optional.of(existingHistory));

    watchProgressService.markCompleted(email, episodeId);

    assertTrue(existingHistory.isCompleted());
    verify(watchHistoryRepository, times(1)).save(existingHistory);
  }

  @Test
  void testMarkCompleted_NoExistingProgress_CreatesCompletedRecord() {
    String email = "test@example.com";
    UUID episodeId = UUID.randomUUID();

    Series series = new Series("Test Series", "Description", "Tech", "thumb.jpg");
    Episode episode = new Episode(series, "Episode 1", "video.mp4", 600, 1);

    when(watchHistoryRepository.findByUserEmailAndEpisodeId(email, episodeId))
        .thenReturn(Optional.empty());
    when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode));

    watchProgressService.markCompleted(email, episodeId);

    verify(watchHistoryRepository, times(1)).save(any(WatchHistory.class));
  }

  @Test
  void testSaveProgress_NullProgress_DefaultsToZero() {
    String email = "test@example.com";
    UUID episodeId = UUID.randomUUID();

    Series series = new Series("Test Series", "Description", "Tech", "thumb.jpg");
    Episode episode = new Episode(series, "Episode 1", "video.mp4", 600, 1);

    when(watchHistoryRepository.findByUserEmailAndEpisodeId(email, episodeId))
        .thenReturn(Optional.empty());
    when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode));

    watchProgressService.saveProgress(email, episodeId, null);

    verify(watchHistoryRepository, times(1))
        .save(argThat(history -> history.getProgressSeconds() == 0));
  }

  @Test
  void testSaveProgress_EpisodeNotFound_ThrowsException() {
    String email = "test@example.com";
    UUID episodeId = UUID.randomUUID();

    when(episodeRepository.findById(episodeId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> watchProgressService.saveProgress(email, episodeId, 120));
  }

  @Test
  void testMarkCompleted_EpisodeNotFound_ThrowsException() {
    String email = "test@example.com";
    UUID episodeId = UUID.randomUUID();

    when(watchHistoryRepository.findByUserEmailAndEpisodeId(email, episodeId))
        .thenReturn(Optional.empty());
    when(episodeRepository.findById(episodeId)).thenReturn(Optional.empty());

    assertThrows(
        ResourceNotFoundException.class,
        () -> watchProgressService.markCompleted(email, episodeId));
  }

  @Test
  void testSaveProgress_ConcurrentInterleavedUpdates() {
    String email = "test@example.com";
    UUID episodeId = UUID.randomUUID();
    UUID seriesId = UUID.randomUUID();

    Series series = new Series("Test Series", "Description", "Tech", "thumb.jpg");
    Episode episode = new Episode(series, "Episode 1", "video.mp4", 600, 1);

    WatchHistory initialHistory = new WatchHistory(email, seriesId, episodeId, 100, false);

    when(watchHistoryRepository.findByUserEmailAndEpisodeId(email, episodeId))
        .thenReturn(Optional.of(initialHistory));
    when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode));

    // Simulate Session A updating progress
    watchProgressService.saveProgress(email, episodeId, 110);
    assertEquals(110, initialHistory.getProgressSeconds());
    verify(watchHistoryRepository, times(1)).save(initialHistory);

    // Simulate Session B updating progress (even if "older" time-wise, distinct
    // request wins)
    watchProgressService.saveProgress(email, episodeId, 105);
    assertEquals(105, initialHistory.getProgressSeconds());
    verify(watchHistoryRepository, times(2)).save(initialHistory);
  }
}
