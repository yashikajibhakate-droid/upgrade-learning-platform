package com.example.app.service;

import com.example.app.dto.ContinueWatchingResponse;
import com.example.app.exception.ResourceNotFoundException;
import com.example.app.model.Episode;
import com.example.app.model.Series;
import com.example.app.model.WatchHistory;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.WatchHistoryRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class WatchProgressService {

  private final WatchHistoryRepository watchHistoryRepository;
  private final EpisodeRepository episodeRepository;

  public WatchProgressService(
      WatchHistoryRepository watchHistoryRepository, EpisodeRepository episodeRepository) {
    this.watchHistoryRepository = watchHistoryRepository;
    this.episodeRepository = episodeRepository;
  }

  @org.springframework.transaction.annotation.Transactional(readOnly = true)
  public Optional<ContinueWatchingResponse> getContinueWatching(String userEmail) {
    Optional<WatchHistory> incompleteWatch = watchHistoryRepository
        .findTop1ByUserEmailAndIsCompletedFalseOrderByLastWatchedAtDesc(
            userEmail);

    if (incompleteWatch.isEmpty()) {
      return Optional.empty();
    }

    WatchHistory watchHistory = incompleteWatch.get();
    Optional<Episode> episode = episodeRepository.findById(watchHistory.getEpisodeId());

    if (episode.isEmpty()) {
      return Optional.empty();
    }

    Episode ep = episode.get();
    Series series = ep.getSeries();

    ContinueWatchingResponse response = new ContinueWatchingResponse(
        series.getId(),
        series.getTitle(),
        series.getThumbnailUrl(),
        series.getCategory(),
        ep.getId(),
        ep.getTitle(),
        ep.getSequenceNumber(),
        ep.getDurationSeconds(),
        ep.getVideoUrl(),
        watchHistory.getProgressSeconds(),
        watchHistory.getLastWatchedAt());

    return Optional.of(response);
  }

  public void saveProgress(String userEmail, UUID episodeId, Integer progressSeconds) {
    if (progressSeconds == null) {
      progressSeconds = 0;
    }

    Optional<WatchHistory> existing = watchHistoryRepository.findByUserEmailAndEpisodeId(userEmail, episodeId);

    // Fetch episode to get duration for clamping
    Episode episode = episodeRepository
        .findById(episodeId)
        .orElseThrow(
            () -> new ResourceNotFoundException("Episode not found with id: " + episodeId));

    // Clamp progress to duration
    int duration = episode.getDurationSeconds();
    int clampedProgress = Math.min(progressSeconds, duration);

    if (existing.isPresent()) {
      WatchHistory watchHistory = existing.get();
      watchHistory.setProgressSeconds(clampedProgress);
      watchHistory.setLastWatchedAt(LocalDateTime.now());
      watchHistoryRepository.save(watchHistory);
    } else {
      WatchHistory watchHistory = new WatchHistory(
          userEmail, episode.getSeries().getId(), episodeId, clampedProgress, false);
      watchHistory.setLastWatchedAt(LocalDateTime.now());
      watchHistoryRepository.save(watchHistory);
    }
  }

  public void markCompleted(String userEmail, UUID episodeId) {
    Optional<WatchHistory> existing = watchHistoryRepository.findByUserEmailAndEpisodeId(userEmail, episodeId);

    if (existing.isPresent()) {
      WatchHistory watchHistory = existing.get();
      watchHistory.setCompleted(true);
      watchHistory.setLastWatchedAt(LocalDateTime.now());
      watchHistoryRepository.save(watchHistory);
    } else {
      Episode episode = episodeRepository
          .findById(episodeId)
          .orElseThrow(
              () -> new ResourceNotFoundException("Episode not found with id: " + episodeId));

      WatchHistory watchHistory = new WatchHistory(userEmail, episode.getSeries().getId(), episodeId, null, true);
      watchHistory.setLastWatchedAt(LocalDateTime.now());
      watchHistoryRepository.save(watchHistory);
    }
  }

  public boolean isEpisodeCompleted(String userEmail, UUID episodeId) {
    Optional<WatchHistory> history = watchHistoryRepository.findByUserEmailAndEpisodeId(userEmail, episodeId);
    return history.isPresent() && history.get().isCompleted();
  }

  public double calculateSeriesProgress(String userEmail, UUID seriesId) {
    List<Episode> episodes = episodeRepository.findBySeriesIdOrderBySequenceNumberAsc(seriesId);
    if (episodes.isEmpty()) {
      return 0.0;
    }

    List<WatchHistory> history = watchHistoryRepository.findByUserEmailAndSeriesId(userEmail, seriesId);

    long completedCount = history.stream().filter(WatchHistory::isCompleted).count();

    // Also consider progress in the current incomplete episode
    double partialProgress = 0.0;
    for (WatchHistory h : history) {
      if (!h.isCompleted() && h.getProgressSeconds() != null) {
        Optional<Episode> ep = episodes.stream().filter(e -> e.getId().equals(h.getEpisodeId())).findFirst();
        if (ep.isPresent() && ep.get().getDurationSeconds() > 0) {
          partialProgress = (double) h.getProgressSeconds() / ep.get().getDurationSeconds();
          break; // Stop after first relevant incomplete episode
        }
      }
    }

    return (completedCount + partialProgress) / episodes.size() * 100.0;
  }
}
