package com.example.app.service;

import com.example.app.dto.ContinueWatchingResponse;
import com.example.app.model.Episode;
import com.example.app.model.Series;
import com.example.app.model.WatchHistory;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.WatchHistoryRepository;
import java.time.LocalDateTime;
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

  public Optional<ContinueWatchingResponse> getContinueWatching(String userEmail) {
    Optional<WatchHistory> incompleteWatch =
        watchHistoryRepository.findTop1ByUserEmailAndIsCompletedFalseOrderByLastWatchedAtDesc(
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

    ContinueWatchingResponse response =
        new ContinueWatchingResponse(
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
    Optional<WatchHistory> existing =
        watchHistoryRepository.findByUserEmailAndEpisodeId(userEmail, episodeId);

    if (existing.isPresent()) {
      WatchHistory watchHistory = existing.get();
      watchHistory.setProgressSeconds(progressSeconds);
      watchHistory.setLastWatchedAt(LocalDateTime.now());
      watchHistoryRepository.save(watchHistory);
    } else {
      Optional<Episode> episode = episodeRepository.findById(episodeId);
      if (episode.isPresent()) {
        WatchHistory watchHistory =
            new WatchHistory(
                userEmail, episode.get().getSeries().getId(), episodeId, progressSeconds, false);
        watchHistory.setLastWatchedAt(LocalDateTime.now());
        watchHistoryRepository.save(watchHistory);
      }
    }
  }

  public void markCompleted(String userEmail, UUID episodeId) {
    Optional<WatchHistory> existing =
        watchHistoryRepository.findByUserEmailAndEpisodeId(userEmail, episodeId);

    if (existing.isPresent()) {
      WatchHistory watchHistory = existing.get();
      watchHistory.setCompleted(true);
      watchHistory.setLastWatchedAt(LocalDateTime.now());
      watchHistoryRepository.save(watchHistory);
    } else {
      Optional<Episode> episode = episodeRepository.findById(episodeId);
      if (episode.isPresent()) {
        WatchHistory watchHistory =
            new WatchHistory(userEmail, episode.get().getSeries().getId(), episodeId, null, true);
        watchHistory.setLastWatchedAt(LocalDateTime.now());
        watchHistoryRepository.save(watchHistory);
      }
    }
  }

  public boolean isEpisodeCompleted(String userEmail, UUID episodeId) {
    Optional<WatchHistory> history =
        watchHistoryRepository.findByUserEmailAndEpisodeId(userEmail, episodeId);
    return history.isPresent() && history.get().isCompleted();
  }
}
