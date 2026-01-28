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
    // Simple check by title to avoid duplicates or update existing
    Series series =
        seriesRepository.findAll().stream()
            .filter(s -> s.getTitle().equals(title))
            .findFirst()
            .orElse(null);

    if (series == null) {
      series = new Series(title, description, category, imageUrl);
      series = seriesRepository.save(series);

      episodeRepository.save(
          new Episode(series, "Episode 1: Intro", "http://example.com/video1", 600, 1));
      episodeRepository.save(
          new Episode(series, "Episode 2: Deep Dive", "http://example.com/video2", 1200, 2));
      episodeRepository.save(
          new Episode(series, "Episode 3: Conclusion", "http://example.com/video3", 900, 3));
    } else {
      // Update image if it changed (or just always update for this task)
      series.setThumbnailUrl(imageUrl);
      seriesRepository.save(series);
    }
  }
}
