package com.example.app.service;

import com.example.app.dto.RecommendationResponse;
import com.example.app.model.Series;
import com.example.app.model.User;
import com.example.app.model.WatchHistory;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.SeriesRepository;
import com.example.app.repository.UserRepository;
import com.example.app.repository.WatchHistoryRepository;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeriesService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private SeriesRepository seriesRepository;

  @Autowired
  private WatchHistoryRepository watchHistoryRepository;

  @Autowired
  private EpisodeRepository episodeRepository;

  public RecommendationResponse getRecommendations(String email) {
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isEmpty()) {
      return new RecommendationResponse(List.of(), List.of());
    }

    User user = userOpt.get();
    Set<String> interests = user.getInterests();

    if (interests == null || interests.isEmpty()) {
      List<Series> all = seriesRepository.findAll();
      return new RecommendationResponse(List.of(), all);
    }

    // 1. Fetch matching series
    List<Series> matchingSeries = seriesRepository.findByCategoryIn(interests);

    // 2. Filter out 100% completed series
    // Optimization: Fetch all watch history for user once
    List<WatchHistory> userHistory = watchHistoryRepository.findByUserEmailAndIsCompletedTrue(email);

    // Map seriesId -> count of completed episodes
    Map<UUID, Long> completedCounts = userHistory.stream()
        .collect(Collectors.groupingBy(WatchHistory::getSeriesId, Collectors.counting()));

    List<Series> filteredRecommended = matchingSeries.stream()
        .filter(
            series -> !isSeriesCompleted(series, completedCounts.getOrDefault(series.getId(), 0L)))
        .collect(Collectors.toList());

    // 3. Fetch "others" (not in interests)
    List<Series> otherSeries = seriesRepository.findByCategoryNotIn(interests);

    return new RecommendationResponse(filteredRecommended, otherSeries);
  }

  public List<com.example.app.model.Episode> getEpisodesForSeries(UUID seriesId) {
    return episodeRepository.findBySeriesIdOrderBySequenceNumberAsc(seriesId);
  }

  private boolean isSeriesCompleted(Series series, long userCompletedCount) {
    if (userCompletedCount == 0)
      return false;

    // This is N+1, but assuming small number of recommended series, it's acceptable
    // for now.
    // Better: Pre-fetch episode counts for all candidate series.
    long totalEpisodes = episodeRepository.findBySeriesIdOrderBySequenceNumberAsc(series.getId()).size();

    return totalEpisodes > 0 && userCompletedCount >= totalEpisodes;
  }

  public Series getSeriesById(UUID id) {
    return seriesRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Series not found"));
  }
}
