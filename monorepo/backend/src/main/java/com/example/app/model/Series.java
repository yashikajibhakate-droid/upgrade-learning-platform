package com.example.app.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "series")
public class Series {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(nullable = false)
  private String title;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false)
  private String category; // Matches user interests (e.g., "Python Programming")

  private String thumbnailUrl;

  public Series() {}

  public Series(String title, String description, String category, String thumbnailUrl) {
    this.title = title;
    this.description = description;
    this.category = category;
    this.thumbnailUrl = thumbnailUrl;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }
}
