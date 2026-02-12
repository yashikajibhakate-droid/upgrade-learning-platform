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

    @Transactional
    public void ingestContent(IngestRequest request) {
        // 1. Handle Series
        Series series;
        if (request.getSeriesId() != null) {
            series = seriesRepository
                    .findById(request.getSeriesId())
                    .orElseGet(() -> createNewSeries(request));
        } else {
            // Try to find by title to avoid duplicates if no ID provided
            // This is a basic check, ideally we should have a unique constraint or slug
            // For now we assume if ID is null we check by title or create new
            // NOTE: SeriesRepository might not have findByTitle, so we might receive an
            // error or need to add it.
            // Checking repository definition in next steps, but assuming distinct titles
            // for now.
            // If findByTitle doesn't exist, we'll create new.
            // Actually, to update existing series without ID, we'd need a way to lookup.
            // For simplicity and per "No Over-Engineering", let's create a new one if ID is
            // null,
            // unless we can easily check.
            // Let's assume we create new if ID is null for now, or users must provide ID to
            // update.
            // Wait, "Duplicate checks: repeat pushes of the same episode ID should update
            // fields".
            // It doesn't explicitly say about Series title.
            // let's stick to: ID provided -> Update, ID null -> Create.
            series = createNewSeries(request);
        }

        // Update series fields
        updateSeriesFields(series, request);
        series = seriesRepository.save(series);

        // 2. Handle Episodes
        for (IngestEpisodeRequest episodeRequest : request.getEpisodes()) {
            handleEpisode(series, episodeRequest);
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
                if (e.getSequenceNumber().equals(request.getSequenceNumber())) {
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
