package com.example.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.app.dto.IngestEpisodeRequest;
import com.example.app.dto.IngestMCQOptionRequest;
import com.example.app.dto.IngestMCQRequest;
import com.example.app.dto.IngestRequest;
import com.example.app.service.IngestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(IngestionController.class)
public class IngestionControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private IngestionService ingestionService;

  @MockBean private com.example.app.service.AuthService authService;

  @Autowired private ObjectMapper objectMapper;

  private IngestRequest ingestRequest;

  @BeforeEach
  void setUp() {
    ingestRequest = new IngestRequest();
    ingestRequest.setSeriesTitle("Test Series");
    ingestRequest.setSeriesDescription("Description");
    ingestRequest.setSeriesCategory("Category");
    // Thumbnail URL is optional in logic but DTO validation?
    // Let's check DTO. IngestRequest has @NotBlank on seriesTitle, seriesCategory.

    IngestEpisodeRequest episodeRequest = new IngestEpisodeRequest();
    episodeRequest.setTitle("Test Episode");
    episodeRequest.setVideoUrl("http://video.url");
    episodeRequest.setDurationSeconds(60);
    episodeRequest.setSequenceNumber(1);

    IngestMCQRequest mcqRequest = new IngestMCQRequest();
    mcqRequest.setQuestion("Test Question");

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
  void testIngestContentSuccess() throws Exception {
    com.example.app.model.User user = new com.example.app.model.User("test@example.com");
    com.example.app.model.Session session = new com.example.app.model.Session(user, "hash");

    doNothing().when(ingestionService).ingestContent(any(IngestRequest.class));
    when(authService.getSession("test-token")).thenReturn(java.util.Optional.of(session));

    mockMvc
        .perform(
            post("/api/ingest")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ingestRequest)))
        .andExpect(status().isCreated());
  }

  @Test
  void testIngestContentBadRequest() throws Exception {
    com.example.app.model.User user = new com.example.app.model.User("test@example.com");
    com.example.app.model.Session session = new com.example.app.model.Session(user, "hash");

    when(authService.getSession("test-token")).thenReturn(java.util.Optional.of(session));

    ingestRequest.setSeriesTitle(null); // Invalid request

    mockMvc
        .perform(
            post("/api/ingest")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(ingestRequest)))
        .andExpect(status().isBadRequest());
  }
}
