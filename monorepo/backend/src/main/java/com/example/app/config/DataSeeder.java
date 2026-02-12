package com.example.app.config;

import com.example.app.model.Episode;
import com.example.app.model.MCQ;
import com.example.app.model.MCQOption;
import com.example.app.model.Series;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.MCQOptionRepository;
import com.example.app.repository.MCQRepository;
import com.example.app.repository.SeriesRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

  @Autowired private SeriesRepository seriesRepository;

  @Autowired private EpisodeRepository episodeRepository;

  @Autowired private MCQRepository mcqRepository;

  @Autowired private MCQOptionRepository mcqOptionRepository;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    System.out.println("Seeding database with initial series and episodes...");

    createOrUpdateSeries(
        "Mastering Python",
        "In-depth guide to Python.",
        "Python Programming",
        "https://images.unsplash.com/photo-1526379095098-d400fd0bf935?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80");

    createOrUpdateSeries(
        "Data Science 101",
        "Learn data analysis.",
        "Data Science",
        "https://images.unsplash.com/photo-1551288049-bebda4e38f71?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80");

    createOrUpdateSeries(
        "UI Design Principles",
        "Beautiful UIs.",
        "UI/UX Design",
        "https://images.unsplash.com/photo-1586717791821-3f44a5638d48?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80");

    createOrUpdateSeries(
        "React for Beginners",
        "Build web apps.",
        "React Framework",
        "https://images.unsplash.com/photo-1633356122544-f134324a6cee?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80");

    createOrUpdateSeries(
        "Cloud AWS",
        "AWS Basics.",
        "Cloud Computing",
        "https://images.unsplash.com/photo-1451187580459-43490279c0fa?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80");

    createOrUpdateSeries(
        "Cybersecurity Basics",
        "Protect systems.",
        "Cybersecurity",
        "https://images.unsplash.com/photo-1614064641938-3bbee52942c7?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80");

    createOrUpdateSeries(
        "Digital Marketing Pro",
        "SEO and Ads.",
        "Digital Marketing",
        "https://images.unsplash.com/photo-1533750516457-a7f992034fec?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80");

    createOrUpdateSeries(
        "Finance Freedom",
        "Investing 101.",
        "Personal Finance",
        "https://images.unsplash.com/photo-1579621970563-ebec7560ff3e?ixlib=rb-4.0.3&auto=format&fit=crop&w=800&q=80");

    System.out.println("Seeding MCQs for all episodes...");
    seedMCQs();

    System.out.println("Seeding completed.");
  }

  private void createOrUpdateSeries(
      String title, String description, String category, String imageUrl) {

    Series series =
        seriesRepository.findAll().stream()
            .filter(s -> s.getTitle().equals(title))
            .findFirst()
            .orElse(null);

    if (series == null) {
      series = new Series(title, description, category, imageUrl);
    } else {
      series.setThumbnailUrl(imageUrl);
    }
    series = seriesRepository.save(series);

    // Ensure episodes exist or update them
    ensureEpisodes(series);
  }

  private void ensureEpisodes(Series series) {
    java.util.List<Episode> existingEpisodes =
        episodeRepository.findBySeriesIdOrderBySequenceNumberAsc(series.getId());

    // oceans.mp4 is 46 seconds
    createOrUpdateEpisode(
        series, existingEpisodes, 1, "Episode 1: Intro", "https://vjs.zencdn.net/v/oceans.mp4", 46);
    // sintel trailer is 52 seconds
    createOrUpdateEpisode(
        series,
        existingEpisodes,
        2,
        "Episode 2: Deep Dive",
        "https://media.w3.org/2010/05/sintel/trailer.mp4",
        52);
    // movie.mp4 is 12 seconds
    createOrUpdateEpisode(
        series,
        existingEpisodes,
        3,
        "Episode 3: Conclusion",
        "https://www.w3schools.com/html/movie.mp4",
        12);
  }

  private void createOrUpdateEpisode(
      Series series,
      java.util.List<Episode> existingEpisodes,
      int sequenceNumber,
      String title,
      String videoUrl,
      int duration) {
    Episode episode =
        existingEpisodes.stream()
            .filter(
                e -> {
                  Integer seq = e.getSequenceNumber();
                  return seq != null && seq == sequenceNumber;
                })
            .findFirst()
            .orElse(null);

    if (episode == null) {
      episode = new Episode(series, title, videoUrl, duration, sequenceNumber);
    } else {
      episode.setTitle(title);
      episode.setVideoUrl(videoUrl);
      episode.setDurationSeconds(duration);
    }
    episodeRepository.save(episode);
  }

  private void seedMCQs() {
    // Get all episodes
    java.util.List<Episode> allEpisodes = episodeRepository.findAll();
    int mcqCreatedCount = 0;

    for (Episode episode : allEpisodes) {
      // Check if MCQ already exists for this episode
      java.util.Optional<MCQ> existingMcq = mcqRepository.findByEpisodeId(episode.getId());
      if (existingMcq.isPresent()) {
        MCQ mcq = existingMcq.get();
        mcq.setRefresherVideoUrl("https://vjs.zencdn.net/v/oceans.mp4");
        mcqRepository.save(mcq);
        continue;
      }

      // Create MCQ based on episode sequence number
      String question;
      String correctAnswer;
      String wrongAnswer1;
      String wrongAnswer2;

      switch (episode.getSequenceNumber()) {
        case 1:
          question = "What is the main concept introduced in this introductory episode?";
          correctAnswer = "Understanding the fundamental principles";
          wrongAnswer1 = "Advanced optimization techniques";
          wrongAnswer2 = "Final implementation details";
          break;
        case 2:
          question = "What key insight does this deep dive episode provide?";
          correctAnswer = "Practical application of core concepts";
          wrongAnswer1 = "Surface-level overview only";
          wrongAnswer2 = "Unrelated theoretical frameworks";
          break;
        case 3:
          question = "What is the main takeaway from this conclusion?";
          correctAnswer = "Synthesizing all learned concepts";
          wrongAnswer1 = "Starting from basics again";
          wrongAnswer2 = "Introducing entirely new topics";
          break;
        default:
          question = "What did you learn in this episode?";
          correctAnswer = "Key concepts and practical applications";
          wrongAnswer1 = "Nothing significant";
          wrongAnswer2 = "Only theoretical background";
      }

      // Create and save MCQ
      MCQ mcq = new MCQ(episode.getId(), question, "https://vjs.zencdn.net/v/oceans.mp4");
      mcq = mcqRepository.save(mcq);

      // Create and save options
      // Create and save options with randomized order
      List<MCQOption> options = new ArrayList<>();
      options.add(new MCQOption(mcq, correctAnswer, true, 0));
      options.add(new MCQOption(mcq, wrongAnswer1, false, 0));
      options.add(new MCQOption(mcq, wrongAnswer2, false, 0));

      Collections.shuffle(options);

      for (int i = 0; i < options.size(); i++) {
        MCQOption option = options.get(i);
        option.setSequenceNumber(i + 1);
        mcqOptionRepository.save(option);
      }

      System.out.println(
          "Created MCQ for episode: " + episode.getTitle() + " (ID: " + episode.getId() + ")");
      mcqCreatedCount++;
    }

    System.out.println("MCQ seeding completed. Created MCQs for " + mcqCreatedCount + " episodes.");
  }
}
