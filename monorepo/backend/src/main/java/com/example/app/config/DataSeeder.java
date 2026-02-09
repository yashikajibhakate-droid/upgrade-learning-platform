package com.example.app.config;

import com.example.app.model.Episode;
import com.example.app.model.Series;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.SeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

  @Autowired private SeriesRepository seriesRepository;

  @Autowired private EpisodeRepository episodeRepository;

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

    // BigBuckBunny.mp4 is 596 seconds (9:56)
    createOrUpdateEpisode(
        series,
        existingEpisodes,
        1,
        "Episode 1: Intro",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
        596);
    // ElephantsDream.mp4 is 653 seconds (10:53)
    createOrUpdateEpisode(
        series,
        existingEpisodes,
        2,
        "Episode 2: Deep Dive",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
        653);
    // ForBiggerBlazes.mp4 is 15 seconds
    createOrUpdateEpisode(
        series,
        existingEpisodes,
        3,
        "Episode 3: Conclusion",
        "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
        15);
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
}
