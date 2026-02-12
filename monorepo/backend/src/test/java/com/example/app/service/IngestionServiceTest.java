package com.example.app.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.app.dto.IngestEpisodeRequest;
import com.example.app.dto.IngestMCQOptionRequest;
import com.example.app.dto.IngestMCQRequest;
import com.example.app.dto.IngestRequest;
import com.example.app.model.Episode;
import com.example.app.model.MCQ;
import com.example.app.model.Series;
import com.example.app.repository.EpisodeRepository;
import com.example.app.repository.MCQRepository;
import com.example.app.repository.SeriesRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IngestionServiceTest {

  @Mock
  private SeriesRepository seriesRepository;

  @Mock
  private EpisodeRepository episodeRepository;

  @Mock
  private MCQRepository mcqRepository;

  @InjectMocks
  private IngestionService ingestionService;

  private IngestRequest ingestRequest;

  @BeforeEach
  void setUp() {
    ingestRequest = new IngestRequest();
    ingestRequest.setSeriesTitle("Test Series");
    ingestRequest.setSeriesDescription("Description");
    ingestRequest.setSeriesCategory("Category");
    ingestRequest.setSeriesThumbnailUrl("http://thumb.url");

    IngestEpisodeRequest episodeRequest = new IngestEpisodeRequest();
    episodeRequest.setTitle("Test Episode");
    episodeRequest.setVideoUrl("http://video.url");
    episodeRequest.setDurationSeconds(60);
    episodeRequest.setSequenceNumber(1);

    IngestMCQRequest mcqRequest = new IngestMCQRequest();
    mcqRequest.setQuestion("Test Question");
    mcqRequest.setRefresherVideoUrl("http://refresher.url");

    List<IngestMCQOptionRequest> options = new ArrayList<>();
    IngestMCQOptionRequest option = new IngestMCQOptionRequest();
    option.setOptionText("Option 1");
    option.setIsCorrect(true);
    option.setSequenceNumber(1);
    options.add(option);
    mcqRequest.setOptions(options);

    episodeRequest.setMcq(mcqRequest);
    ingestRequest.setEpisodes(List.of(episodeRequest));
  }

  @Test
  void testIngestNewContent() {
    when(seriesRepository.save(any(Series.class)))
        .thenAnswer(
            i -> {
              Series s = i.getArgument(0);
              s.setId(UUID.randomUUID());
              return s;
            });

    when(episodeRepository.save(any(Episode.class)))
        .thenAnswer(
            i -> {
              Episode e = i.getArgument(0);
              return e; // id is auto-generated usually, but here we mock
            });

    // Mock finding existing episodes (empty list for new content)
    when(episodeRepository.findBySeriesIdOrderBySequenceNumberAsc(any(UUID.class)))
        .thenReturn(new ArrayList<>());

    when(mcqRepository.findByEpisodeId(any())).thenReturn(Optional.empty());

    ingestionService.ingestContent(ingestRequest);

    verify(seriesRepository, times(1)).save(any(Series.class));
    verify(episodeRepository, times(1)).save(any(Episode.class));
    verify(mcqRepository, times(1)).save(any(MCQ.class));
  }

  @Test
  void testIngestUpdateExistingSeries() {
    UUID seriesId = UUID.randomUUID();
    ingestRequest.setSeriesId(seriesId);

    Series existingSeries = new Series();
    existingSeries.setId(seriesId);

    when(seriesRepository.findById(seriesId)).thenReturn(Optional.of(existingSeries));
    when(seriesRepository.save(any(Series.class))).thenReturn(existingSeries);
    when(episodeRepository.findBySeriesIdOrderBySequenceNumberAsc(seriesId))
        .thenReturn(new ArrayList<>());

    when(episodeRepository.save(any(Episode.class))).thenAnswer(i -> i.getArgument(0));
    when(mcqRepository.findByEpisodeId(any())).thenReturn(Optional.empty());

    ingestionService.ingestContent(ingestRequest);

    verify(seriesRepository, times(1)).findById(seriesId);
    verify(seriesRepository, times(1)).save(existingSeries);
  }

  @Test
  void testIngestWithUnknownSeriesId_ShouldThrowException() {
    UUID unknownId = UUID.randomUUID();
    ingestRequest.setSeriesId(unknownId);

    when(seriesRepository.findById(unknownId)).thenReturn(Optional.empty());

    org.junit.jupiter.api.Assertions.assertThrows(
        com.example.app.exception.ResourceNotFoundException.class,
        () -> ingestionService.ingestContent(ingestRequest));

    verify(seriesRepository).findById(unknownId);
    verify(seriesRepository, never()).save(any());
  }

  @Test
  void testIngestWithNullEpisodes_ShouldSucceed() {
    ingestRequest.setEpisodes(null);

    when(seriesRepository.save(any(Series.class)))
        .thenAnswer(
            i -> {
              Series s = i.getArgument(0);
              s.setId(UUID.randomUUID());
              return s;
            });

    org.junit.jupiter.api.Assertions.assertDoesNotThrow(
        () -> ingestionService.ingestContent(ingestRequest));

    verify(seriesRepository, times(1)).save(any(Series.class));
    verify(episodeRepository, never()).save(any());
  }
}
