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

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DailyReminderJob.class);

    @Autowired
    public DailyReminderJob(
            UserRepository userRepository,
            WatchProgressService watchProgressService,
            SeriesService seriesService,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.watchProgressService = watchProgressService;
        this.seriesService = seriesService;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void sendDailyReminders() {
        logger.info("Starting daily reminder job...");
        List<User> users = userRepository.findAll();

        for (User user : users) {
            try {
                processUserReminder(user);
            } catch (Exception e) {
                logger.error("Failed to process daily reminder for user: {}", user.getEmail(), e);
            }
        }
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

        // Fallback: send top series from "other" if no personalized recommendations?
        // User request says "Else show top interest-based recommendations"
        // If interest-based is empty, maybe we should act on "others"?
        // For now, adhering strictly to "interest-based recommendations" per scope.
        // If no interests, the original logic in SeriesService returns all series
        // (excluding continued).
        // So if getRecommended is empty, it means no series available at all or
        // something.
        // Let's check "others" just in case getting filtered out.

        if (recommendations != null && recommendations.getOthers() != null && !recommendations.getOthers().isEmpty()) {
            // Should we send generic recommendation? Scope says "top interest-based
            // recommendations".
            // If user has NO interests, SeriesService.getRecommendations returns empty
            // "recommended" list?
            // Let's check SeriesService source again.
            // If interests is empty, it returns empty "recommended" and all series in
            // "others" (actually wait, code said in line 58:
            // RecommendationResponse(List.of(), all) IS WRONG?
            // user has no interests -> line 50: if (interests == null ||
            // interests.isEmpty()) { ... return new RecommendationResponse(List.of(), all);
            // }
            // Wait, the constructor is (recommended, other).
            // So if no interests, "recommended" is empty, "other" has everything.
            // The requirement "Else show top interest-based recommendations" implies we
            // should show SOMETHING.
            // If "recommended" list is empty, picking from "other" seems like a reasonable
            // fallback to "show top recommendations" (generic).

            sendRecommendationEmail(email, recommendations.getOthers().get(0));
            return;
        }

        logger.info("No active series or recommendations found for user: {}", email);
    }

    private void sendResumeEmail(String email, ContinueWatchingResponse progress) {
        String subject = "Resume your learning: " + progress.getSeriesTitle();
        // Assuming backend shouldn't know about frontend routes ideally, but simple
        // string concat is fine for now.
        String link = frontendUrl + "/series/" + progress.getSeriesId() + "/watch?episodeId=" + progress.getEpisodeId()
                + "&email=" + email;
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
            // logger.warn("Recommended series {} has no episodes.", series.getTitle());
            return;
        }
        Episode firstEpisode = episodes.get(0);

        String subject = "Recommended for you: " + series.getTitle();
        String link = frontendUrl + "/series/" + series.getId() + "/watch?episodeId=" + firstEpisode.getId() + "&email="
                + email;
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
