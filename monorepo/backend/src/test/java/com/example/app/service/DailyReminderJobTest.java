package com.example.app.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.app.dto.ContinueWatchingResponse;
import com.example.app.dto.RecommendationResponse;
import com.example.app.model.Episode;
import com.example.app.model.Series;
import com.example.app.model.User;
import com.example.app.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class DailyReminderJobTest {

        @Mock
        private UserRepository userRepository;
        @Mock
        private WatchProgressService watchProgressService;
        @Mock
        private SeriesService seriesService;
        @Mock
        private EmailService emailService;
        @Mock
        private AuthService authService;

        @InjectMocks
        private DailyReminderJob dailyReminderJob;

        private User testUser;
        private final String TEST_EMAIL = "test@example.com";
        private final String TEST_TOKEN = "magic-token-123";

        @BeforeEach
        void setUp() {
                testUser = new User();
                testUser.setEmail(TEST_EMAIL);
                ReflectionTestUtils.setField(dailyReminderJob, "frontendUrl", "http://localhost:5173");
        }

        @Test
        void shouldSendResumeEmail_WhenContinueWatchingExists() {
                // Arrange
                org.springframework.data.domain.Page<User> page = new org.springframework.data.domain.PageImpl<>(
                                java.util.List.of(testUser));
                when(userRepository.findAll(
                                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(page);

                ContinueWatchingResponse progress = new ContinueWatchingResponse(
                                UUID.randomUUID(),
                                "Test Series",
                                "http://thumb.url",
                                "Tech",
                                UUID.randomUUID(),
                                "Episode 1",
                                1,
                                600,
                                "http://video.url",
                                300,
                                LocalDateTime.now());

                when(watchProgressService.getContinueWatching(TEST_EMAIL)).thenReturn(Optional.of(progress));
                when(authService.generateMagicLinkToken(TEST_EMAIL)).thenReturn(TEST_TOKEN);

                // Act
                dailyReminderJob.sendDailyReminders();

                // Assert
                verify(emailService, times(1))
                                .sendDailyReminder(
                                                eq(TEST_EMAIL),
                                                eq("Resume your learning: " + progress.getSeriesTitle()),
                                                contains("/series/" + progress.getSeriesId() + "/watch?episodeId="
                                                                + progress.getEpisodeId() + "&token=" + TEST_TOKEN));
                verify(seriesService, never()).getRecommendations(anyString());
        }

        @Test
        void shouldSendRecommendationEmail_WhenNoContinueWatchingExists() {
                // Arrange
                org.springframework.data.domain.Page<User> page = new org.springframework.data.domain.PageImpl<>(
                                java.util.List.of(testUser));
                when(userRepository.findAll(
                                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(page);
                when(watchProgressService.getContinueWatching(TEST_EMAIL)).thenReturn(Optional.empty());

                Series recommendedSeries = new Series();
                ReflectionTestUtils.setField(recommendedSeries, "id", UUID.randomUUID());
                recommendedSeries.setTitle("Recommended Series");
                recommendedSeries.setDescription("Great series");

                RecommendationResponse recommendations = new RecommendationResponse(List.of(recommendedSeries),
                                Collections.emptyList());
                when(seriesService.getRecommendations(TEST_EMAIL)).thenReturn(recommendations);
                when(authService.generateMagicLinkToken(TEST_EMAIL)).thenReturn(TEST_TOKEN);

                Episode firstEpisode = new Episode();
                ReflectionTestUtils.setField(firstEpisode, "id", UUID.randomUUID());
                when(seriesService.getEpisodesForSeries(recommendedSeries.getId()))
                                .thenReturn(List.of(firstEpisode));

                // Act
                dailyReminderJob.sendDailyReminders();

                // Assert
                verify(emailService, times(1))
                                .sendDailyReminder(
                                                eq(TEST_EMAIL),
                                                eq("Recommended for you: " + recommendedSeries.getTitle()),
                                                contains("/series/" + recommendedSeries.getId() + "/watch?episodeId="
                                                                + firstEpisode.getId() + "&token=" + TEST_TOKEN));
        }

        @Test
        void shouldSendFallbackRecommendationEmail_WhenMainRecommendationEmpty() {
                // Arrange
                org.springframework.data.domain.Page<User> page = new org.springframework.data.domain.PageImpl<>(
                                java.util.List.of(testUser));
                when(userRepository.findAll(
                                org.mockito.ArgumentMatchers.any(org.springframework.data.domain.Pageable.class)))
                                .thenReturn(page);
                when(watchProgressService.getContinueWatching(TEST_EMAIL)).thenReturn(Optional.empty());

                Series fallbackSeries = new Series();
                ReflectionTestUtils.setField(fallbackSeries, "id", UUID.randomUUID());
                fallbackSeries.setTitle("Fallback Series");
                fallbackSeries.setDescription("Good fallback");

                // Empty recommendations but present "other"
                RecommendationResponse recommendations = new RecommendationResponse(Collections.emptyList(),
                                List.of(fallbackSeries));
                when(seriesService.getRecommendations(TEST_EMAIL)).thenReturn(recommendations);
                when(authService.generateMagicLinkToken(TEST_EMAIL)).thenReturn(TEST_TOKEN);

                Episode firstEpisode = new Episode();
                ReflectionTestUtils.setField(firstEpisode, "id", UUID.randomUUID());
                when(seriesService.getEpisodesForSeries(fallbackSeries.getId()))
                                .thenReturn(List.of(firstEpisode));

                // Act
                dailyReminderJob.sendDailyReminders();

                // Assert
                verify(emailService, times(1))
                                .sendDailyReminder(
                                                eq(TEST_EMAIL),
                                                eq("Recommended for you: " + fallbackSeries.getTitle()),
                                                contains("/series/" + fallbackSeries.getId() + "/watch?episodeId="
                                                                + firstEpisode.getId() + "&token=" + TEST_TOKEN));
        }
}
