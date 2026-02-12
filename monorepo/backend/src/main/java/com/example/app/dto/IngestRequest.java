package com.example.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public class IngestRequest {

  private UUID seriesId;

  @NotBlank(message = "Series title is required")
  private String seriesTitle;

  private String seriesDescription;

  @NotBlank(message = "Series category is required")
  private String seriesCategory;

  private String seriesThumbnailUrl;

  @Valid
  @NotNull(message = "Episodes list is required")
  private List<IngestEpisodeRequest> episodes;

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

  public String getSeriesDescription() {
    return seriesDescription;
  }

  public void setSeriesDescription(String seriesDescription) {
    this.seriesDescription = seriesDescription;
  }

  public String getSeriesCategory() {
    return seriesCategory;
  }

  public void setSeriesCategory(String seriesCategory) {
    this.seriesCategory = seriesCategory;
  }

  public String getSeriesThumbnailUrl() {
    return seriesThumbnailUrl;
  }

  public void setSeriesThumbnailUrl(String seriesThumbnailUrl) {
    this.seriesThumbnailUrl = seriesThumbnailUrl;
  }

  public List<IngestEpisodeRequest> getEpisodes() {
    return episodes;
  }

  public void setEpisodes(List<IngestEpisodeRequest> episodes) {
    this.episodes = episodes;
  }
}
