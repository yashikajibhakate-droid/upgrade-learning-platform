package com.example.app.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "episodes")
public class Episode {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @com.fasterxml.jackson.annotation.JsonIgnore
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "series_id", nullable = false)
  private Series series;

  @Column(nullable = false)
  private String title;

  private String videoUrl;

  private Integer durationSeconds;

  private Integer sequenceNumber;

  public Episode() {
  }

  public Episode(
      Series series,
      String title,
      String videoUrl,
      Integer durationSeconds,
      Integer sequenceNumber) {
    this.series = series;
    this.title = title;
    this.videoUrl = videoUrl;
    this.durationSeconds = durationSeconds;
    this.sequenceNumber = sequenceNumber;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public Series getSeries() {
    return series;
  }

  public void setSeries(Series series) {
    this.series = series;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  public void setVideoUrl(String videoUrl) {
    this.videoUrl = videoUrl;
  }

  public Integer getDurationSeconds() {
    return durationSeconds;
  }

  public void setDurationSeconds(Integer durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  public Integer getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Integer sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }
}
