package com.example.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ContinueWatchingResponse {

  private UUID seriesId;
  private String seriesTitle;
  private String seriesThumbnailUrl;
  private String seriesCategory;

  private UUID episodeId;
  private String episodeTitle;
  private Integer episodeSequenceNumber;
  private Integer episodeDurationSeconds;
  private String episodeVideoUrl;

  private Integer progressSeconds;
  private LocalDateTime lastWatchedAt;

  public ContinueWatchingResponse() {}

  public ContinueWatchingResponse(
      UUID seriesId,
      String seriesTitle,
      String seriesThumbnailUrl,
      String seriesCategory,
      UUID episodeId,
      String episodeTitle,
      Integer episodeSequenceNumber,
      Integer episodeDurationSeconds,
      String episodeVideoUrl,
      Integer progressSeconds,
      LocalDateTime lastWatchedAt) {
    this.seriesId = seriesId;
    this.seriesTitle = seriesTitle;
    this.seriesThumbnailUrl = seriesThumbnailUrl;
    this.seriesCategory = seriesCategory;
    this.episodeId = episodeId;
    this.episodeTitle = episodeTitle;
    this.episodeSequenceNumber = episodeSequenceNumber;
    this.episodeDurationSeconds = episodeDurationSeconds;
    this.episodeVideoUrl = episodeVideoUrl;
    this.progressSeconds = progressSeconds;
    this.lastWatchedAt = lastWatchedAt;
  }

  public UUID getSeriesId() {
    return seriesId;
  }

  public void setSeriesId(UUID seriesId) {
    this.seriesId = seriesId;
  }

  public String getSeriesTitle() {
    return seriesTitle;
  }

  public void setSeriesTitle(String seriesTitle) {
    this.seriesTitle = seriesTitle;
  }

  public String getSeriesThumbnailUrl() {
    return seriesThumbnailUrl;
  }

  public void setSeriesThumbnailUrl(String seriesThumbnailUrl) {
    this.seriesThumbnailUrl = seriesThumbnailUrl;
  }

  public String getSeriesCategory() {
    return seriesCategory;
  }

  public void setSeriesCategory(String seriesCategory) {
    this.seriesCategory = seriesCategory;
  }

  public UUID getEpisodeId() {
    return episodeId;
  }

  public void setEpisodeId(UUID episodeId) {
    this.episodeId = episodeId;
  }

  public String getEpisodeTitle() {
    return episodeTitle;
  }

  public void setEpisodeTitle(String episodeTitle) {
    this.episodeTitle = episodeTitle;
  }

  public Integer getEpisodeSequenceNumber() {
    return episodeSequenceNumber;
  }

  public void setEpisodeSequenceNumber(Integer episodeSequenceNumber) {
    this.episodeSequenceNumber = episodeSequenceNumber;
  }

  public Integer getEpisodeDurationSeconds() {
    return episodeDurationSeconds;
  }

  public void setEpisodeDurationSeconds(Integer episodeDurationSeconds) {
    this.episodeDurationSeconds = episodeDurationSeconds;
  }

  public String getEpisodeVideoUrl() {
    return episodeVideoUrl;
  }

  public void setEpisodeVideoUrl(String episodeVideoUrl) {
    this.episodeVideoUrl = episodeVideoUrl;
  }

  public Integer getProgressSeconds() {
    return progressSeconds;
  }

  public void setProgressSeconds(Integer progressSeconds) {
    this.progressSeconds = progressSeconds;
  }

  public LocalDateTime getLastWatchedAt() {
    return lastWatchedAt;
  }

  public void setLastWatchedAt(LocalDateTime lastWatchedAt) {
    this.lastWatchedAt = lastWatchedAt;
  }
}
