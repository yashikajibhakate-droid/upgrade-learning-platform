package com.example.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "watch_history")
public class WatchHistory {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(nullable = false)
  private String userEmail;

  @Column(nullable = false)
  private UUID seriesId; // Storing ID directly for simpler querying, or could be ManyToOne

  @Column(nullable = false)
  private UUID episodeId;

  private Integer progressSeconds;

  private boolean isCompleted;

  private LocalDateTime lastWatchedAt;

  public WatchHistory() {
  }

  public WatchHistory(
      String userEmail,
      UUID seriesId,
      UUID episodeId,
      Integer progressSeconds,
      boolean isCompleted) {
    this.userEmail = userEmail;
    this.seriesId = seriesId;
    this.episodeId = episodeId;
    this.progressSeconds = progressSeconds;
    this.isCompleted = isCompleted;
    this.lastWatchedAt = LocalDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public UUID getSeriesId() {
    return seriesId;
  }

  public void setSeriesId(UUID seriesId) {
    this.seriesId = seriesId;
  }

  public UUID getEpisodeId() {
    return episodeId;
  }

  public void setEpisodeId(UUID episodeId) {
    this.episodeId = episodeId;
  }

  public Integer getProgressSeconds() {
    return progressSeconds;
  }

  public void setProgressSeconds(Integer progressSeconds) {
    this.progressSeconds = progressSeconds;
  }

  public boolean isCompleted() {
    return isCompleted;
  }

  public void setCompleted(boolean completed) {
    isCompleted = completed;
  }

  public LocalDateTime getLastWatchedAt() {
    return lastWatchedAt;
  }

  public void setLastWatchedAt(LocalDateTime lastWatchedAt) {
    this.lastWatchedAt = lastWatchedAt;
  }
}
