package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.example.app.dto.RecommendationResponse;
import com.example.app.model.Series;
import com.example.app.model.User;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.SeriesRepository;
import com.example.app.repository.UserRepository;
import com.example.app.repository.WatchHistoryRepository;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeriesServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private SeriesRepository seriesRepository;
  @Mock private WatchHistoryRepository watchHistoryRepository;
  @Mock private EpisodeRepository episodeRepository;

  @InjectMocks private SeriesService seriesService;

  @Test
  void testGetRecommendations_Success() {
    String email = "test@example.com";
    User user = new User(email);
    user.setInterests(new HashSet<>(List.of("Tech")));

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    Series series = new Series();
    series.setId(UUID.randomUUID());
    series.setCategory("Tech");

    when(seriesRepository.findByCategoryIn(any())).thenReturn(List.of(series));
    when(watchHistoryRepository.findByUserEmailAndIsCompletedTrue(email))
        .thenReturn(Collections.emptyList());
    when(seriesRepository.findByCategoryNotIn(any())).thenReturn(Collections.emptyList());

    RecommendationResponse response = seriesService.getRecommendations(email);
    assertNotNull(response);
    assertEquals(1, response.getRecommended().size());
  }

  @Test
  void testGetRecommendations_NullUser_ReturnsEmpty() {
    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    RecommendationResponse response = seriesService.getRecommendations("unknown@example.com");
    assertTrue(response.getRecommended().isEmpty());
  }

  @Test
  void testGetRecommendations_NullInterests() {
    String email = "test@example.com";
    User user = new User(email);
    user.setInterests(null); // Explicit null

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
    when(seriesRepository.findAll()).thenReturn(Collections.emptyList());

    RecommendationResponse response = seriesService.getRecommendations(email);
    assertNotNull(response);
  }

  @Test
  void testGetRecommendations_SortsByInterestWeight() {
    String email = "test@example.com";
    User user = new User(email);
    user.setInterests(new HashSet<>(List.of("Python", "Java")));
    // Set weights: Python (10) > Java (5)
    user.getInterestWeights().put("Python", 10);
    user.getInterestWeights().put("Java", 5);

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    Series javaSeries = new Series();
    javaSeries.setId(UUID.randomUUID());
    javaSeries.setTitle("Java Basics");
    javaSeries.setCategory("Java"); // Matched interest, weight 5

    Series pythonSeries = new Series();
    pythonSeries.setId(UUID.randomUUID());
    pythonSeries.setTitle("Python Basics");
    pythonSeries.setCategory("Python"); // Matched interest, weight 10

    // Repository returns them in arbitrary order (e.g. Java first)
    when(seriesRepository.findByCategoryIn(any())).thenReturn(List.of(javaSeries, pythonSeries));

    when(watchHistoryRepository.findByUserEmailAndIsCompletedTrue(email))
        .thenReturn(Collections.emptyList());
    when(seriesRepository.findByCategoryNotIn(any())).thenReturn(Collections.emptyList());

    RecommendationResponse response = seriesService.getRecommendations(email);

    assertNotNull(response);
    List<Series> recommended = response.getRecommended();
    assertEquals(2, recommended.size());

    // Should be Python first (weight 10), then Java (weight 5)
    assertEquals("Python Basics", recommended.get(0).getTitle());
    assertEquals("Java Basics", recommended.get(1).getTitle());
  }
}
