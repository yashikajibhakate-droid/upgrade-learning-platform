package com.example.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class IngestEpisodeRequest {

  private UUID episodeId;

  @NotBlank(message = "Episode title is required")
  private String title;

  @NotBlank(message = "Video URL is required")
  private String videoUrl;

  @NotNull(message = "Duration is required")
  private Integer durationSeconds;

  @NotNull(message = "Sequence number is required")
  private Integer sequenceNumber;

  @Valid private IngestMCQRequest mcq;

  public UUID getEpisodeId() {
    return episodeId;
  }

  public void setEpisodeId(UUID episodeId) {
    this.episodeId = episodeId;
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

  public IngestMCQRequest getMcq() {
    return mcq;
  }

  public void setMcq(IngestMCQRequest mcq) {
    this.mcq = mcq;
  }
}
