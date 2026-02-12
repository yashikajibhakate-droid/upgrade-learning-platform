package com.example.app.service;

import com.example.app.dto.IngestEpisodeRequest;
import com.example.app.dto.IngestMCQOptionRequest;
import com.example.app.dto.IngestMCQRequest;
import com.example.app.dto.IngestRequest;
import com.example.app.model.Episode;
import com.example.app.model.MCQ;
import com.example.app.model.MCQOption;
import com.example.app.model.Series;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.MCQRepository;
import com.example.app.repository.SeriesRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IngestionService {

  private final SeriesRepository seriesRepository;
  private final EpisodeRepository episodeRepository;
  private final MCQRepository mcqRepository;

  public IngestionService(
      SeriesRepository seriesRepository,
      EpisodeRepository episodeRepository,
      MCQRepository mcqRepository) {
    this.seriesRepository = seriesRepository;
    this.episodeRepository = episodeRepository;
    this.mcqRepository = mcqRepository;
  }

  /**
   * Ingests content from the provided request.
   * If request.seriesId is provided, it must exist.
   * Otherwise, if request.seriesId is null, a new series will be created.
   *
   * @param request The ingestion request containing series and episode data.
   * @throws com.example.app.exception.ResourceNotFoundException if seriesId is
   *                                                             provided but not
   *                                                             found.
   */
  @Transactional
  public void ingestContent(IngestRequest request) {
    // 1. Handle Series
    Series series;
    if (request.getSeriesId() != null) {
      series = seriesRepository
          .findById(request.getSeriesId())
          .orElseThrow(
              () -> new com.example.app.exception.ResourceNotFoundException(
                  "Series not found with ID: " + request.getSeriesId()));
    } else {
      series = createNewSeries(request);
    }

    // Update series fields
    updateSeriesFields(series, request);
    series = seriesRepository.save(series);

    // 2. Handle Episodes
    if (request.getEpisodes() != null) {
      for (IngestEpisodeRequest episodeRequest : request.getEpisodes()) {
        handleEpisode(series, episodeRequest);
      }
    }
  }

  private Series createNewSeries(IngestRequest request) {
    Series series = new Series();
    return series;
  }

  private void updateSeriesFields(Series series, IngestRequest request) {
    series.setTitle(request.getSeriesTitle());
    series.setDescription(request.getSeriesDescription());
    series.setCategory(request.getSeriesCategory());
    series.setThumbnailUrl(request.getSeriesThumbnailUrl());
  }

  private void handleEpisode(Series series, IngestEpisodeRequest request) {
    Episode episode = null;

    if (request.getEpisodeId() != null) {
      episode = episodeRepository.findById(request.getEpisodeId()).orElse(null);
    }

    // If not found by ID, check by sequence number in this series to avoid
    // duplicates
    // "Duplicate checks: repeat pushes of the same episode ID should update fields"
    // Also "Duplicate updates... update existing content, without creating
    // duplicates"
    if (episode == null) {
      // Check if episode number exists in this series
      // We need a repository method for this: findBySeriesAndSequenceNumber
      // If not available, we might iterate or use findBySeriesId (which returns list)
      // List<Episode> episodes = episodeRepository.findBySeriesId(series.getId());
      // This is defined in SeriesService as `getEpisodesForSeries`.
      // I'll assume we can define or use an existing method.
      // Let's try to query.
      // For now, I'll fetch all episodes for series and filter.
      List<Episode> existingEpisodes = episodeRepository.findBySeriesIdOrderBySequenceNumberAsc(series.getId());
      for (Episode e : existingEpisodes) {
        if (Objects.equals(e.getSequenceNumber(), request.getSequenceNumber())) {
          episode = e;
          break;
        }
      }
    }

    if (episode == null) {
      episode = new Episode();
      episode.setSeries(series);
    }

    // Update fields
    episode.setTitle(request.getTitle());
    episode.setVideoUrl(request.getVideoUrl());
    episode.setDurationSeconds(request.getDurationSeconds());
    episode.setSequenceNumber(request.getSequenceNumber());

    episode = episodeRepository.save(episode);

    // 3. Handle MCQ
    if (request.getMcq() != null) {
      handleMCQ(episode, request.getMcq());
    }
  }

  private void handleMCQ(Episode episode, IngestMCQRequest mcqRequest) {
    MCQ mcq = mcqRepository.findByEpisodeId(episode.getId()).orElse(new MCQ());
    mcq.setEpisodeId(episode.getId());
    mcq.setQuestion(mcqRequest.getQuestion());
    mcq.setRefresherVideoUrl(mcqRequest.getRefresherVideoUrl());

    // Handle Options
    // Since we are updating, we should probably clear existing and add new,
    // or attempt to update.
    // Replacing list is safer for "Ingestion" where payload is source of truth.
    if (mcq.getOptions() != null) {
      mcq.getOptions().clear();
    } else {
      mcq.setOptions(new ArrayList<>());
    }

    for (IngestMCQOptionRequest optionRequest : mcqRequest.getOptions()) {
      MCQOption option = new MCQOption();
      option.setMcq(mcq);
      option.setOptionText(optionRequest.getOptionText());
      option.setIsCorrect(optionRequest.getIsCorrect());
      option.setSequenceNumber(optionRequest.getSequenceNumber());
      mcq.getOptions().add(option);
    }

    mcqRepository.save(mcq);
  }
}
