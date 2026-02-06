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
}
