package com.example.app.service;

import com.example.app.dto.ContinueWatchingResponse;
import com.example.app.dto.RecommendationResponse;
import com.example.app.model.Episode;
import com.example.app.model.Series;
import com.example.app.model.User;
import com.example.app.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DailyReminderJob {

    private final UserRepository userRepository;
    private final WatchProgressService watchProgressService;
    private final SeriesService seriesService;
    private final EmailService emailService;
    private final AuthService authService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DailyReminderJob.class);

    @Autowired
    public DailyReminderJob(
            UserRepository userRepository,
            WatchProgressService watchProgressService,
            SeriesService seriesService,
            EmailService emailService,
            AuthService authService) {
        this.userRepository = userRepository;
        this.watchProgressService = watchProgressService;
        this.seriesService = seriesService;
        this.emailService = emailService;
        this.authService = authService;
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void sendDailyReminders() {
        logger.info("Starting daily reminder job...");
        int page = 0;
        int size = 100;
        org.springframework.data.domain.Page<User> userPage;

        do {
            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page,
                    size);
            userPage = userRepository.findAll(pageable);

            for (User user : userPage.getContent()) {
                try {
                    processUserReminder(user);
                } catch (Exception e) {
                    logger.error("Failed to process daily reminder for user: {}", user.getEmail(), e);
                }
            }
            page++;
        } while (userPage.hasNext());

        logger.info("Daily reminder job completed.");
    }

    private void processUserReminder(User user) {
        String email = user.getEmail();

        // 1. Check for Continue Watching (Resume)
        Optional<ContinueWatchingResponse> continueWatching = watchProgressService.getContinueWatching(email);

        if (continueWatching.isPresent()) {
            sendResumeEmail(email, continueWatching.get());
            return;
        }

        // 2. Check for Recommendations
        RecommendationResponse recommendations = seriesService.getRecommendations(email);
        // getRecommendations returns non-null RecommendationResponse, but
        // getRecommended() might be empty
        if (recommendations != null && recommendations.getRecommended() != null
                && !recommendations.getRecommended().isEmpty()) {
            sendRecommendationEmail(email, recommendations.getRecommended().get(0));
            return;
        }

        // Fallback to generic recommendations when user-specific recommendations are
        // empty
        if (recommendations != null && recommendations.getOthers() != null && !recommendations.getOthers().isEmpty()) {
            sendRecommendationEmail(email, recommendations.getOthers().get(0));
            return;
        }

        logger.info("No active series or recommendations found for user: {}", email);
    }

    private void sendResumeEmail(String email, ContinueWatchingResponse progress) {
        String subject = "Resume your learning: " + progress.getSeriesTitle();
        String token = authService.generateMagicLinkToken(email);

        String link = frontendUrl + "/series/" + progress.getSeriesId() + "/watch?episodeId=" + progress.getEpisodeId()
                + "&token=" + token;
        String content = String.format(
                "Hi there,\n\n"
                        + "Pick up where you left off! Continue watching \"%s\":\n\n"
                        + "Series: %s\n"
                        + "Episode Number: %d - %s\n\n"
                        + "Click here to resume: %s\n\n"
                        + "Happy Learning!",
                progress.getSeriesTitle(),
                progress.getSeriesTitle(),
                progress.getEpisodeSequenceNumber(),
                progress.getEpisodeTitle(),
                link);

        emailService.sendDailyReminder(email, subject, content);
    }

    private void sendRecommendationEmail(String email, Series series) {
        // Determine the first episode of the recommended series
        List<Episode> episodes = seriesService.getEpisodesForSeries(series.getId());
        if (episodes.isEmpty()) {
            return;
        }
        Episode firstEpisode = episodes.get(0);

        String subject = "Recommended for you: " + series.getTitle();
        String token = authService.generateMagicLinkToken(email);

        String link = frontendUrl + "/series/" + series.getId() + "/watch?episodeId=" + firstEpisode.getId() + "&token="
                + token;
        String content = String.format(
                "Hi there,\n\n"
                        + "Based on your interests, we think you'll love \"%s\"!\n\n"
                        + "Description: %s\n\n"
                        + "Click here to start watching: %s\n\n"
                        + "Happy Learning!",
                series.getTitle(), series.getDescription(), link);

        emailService.sendDailyReminder(email, subject, content);
    }
}
