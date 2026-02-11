package com.example.app.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.app.dto.ContinueWatchingResponse;
import com.example.app.service.WatchProgressService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WatchProgressController.class)
class WatchProgressControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private WatchProgressService watchProgressService;

  // Mocking dependencies required by WebConfig/AuthInterceptor
  @MockBean
  private com.example.app.service.UserService userService;

  @MockBean
  private com.example.app.config.AuthInterceptor authInterceptor;

  @Autowired
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() throws Exception {
    when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
  }

  @Test
  void testGetContinueWatching_WithData_Returns200() throws Exception {
    String email = "test@example.com";
    ContinueWatchingResponse response = new ContinueWatchingResponse(
        UUID.randomUUID(),
        "Test Series",
        "thumb.jpg",
        "Tech",
        UUID.randomUUID(),
        "Episode 1",
        1,
        600,
        "video.mp4",
        120,
        LocalDateTime.now());

    when(watchProgressService.getContinueWatching(email)).thenReturn(Optional.of(response));

    mockMvc
        .perform(get("/api/watch-progress/continue").param("email", email))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.seriesTitle").value("Test Series"))
        .andExpect(jsonPath("$.episodeTitle").value("Episode 1"))
        .andExpect(jsonPath("$.progressSeconds").value(120));
  }

  @Test
  void testGetContinueWatching_NoData_Returns404() throws Exception {
    String email = "test@example.com";

    when(watchProgressService.getContinueWatching(email)).thenReturn(Optional.empty());

    mockMvc
        .perform(get("/api/watch-progress/continue").param("email", email))
        .andExpect(status().isNotFound());
  }

  @Test
  void testSaveProgress_ReturnsSuccess() throws Exception {
    Map<String, Object> request = new HashMap<>();
    request.put("email", "test@example.com");
    request.put("episodeId", UUID.randomUUID().toString());
    request.put("progressSeconds", 120);

    doNothing().when(watchProgressService).saveProgress(anyString(), any(UUID.class), anyInt(), any());

    mockMvc
        .perform(
            post("/api/watch-progress/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Progress saved successfully"));
  }

  @Test
  void testMarkCompleted_ReturnsSuccess() throws Exception {
    Map<String, Object> request = new HashMap<>();
    request.put("email", "test@example.com");
    request.put("episodeId", UUID.randomUUID().toString());

    doNothing().when(watchProgressService).markCompleted(anyString(), any(UUID.class));

    mockMvc
        .perform(
            post("/api/watch-progress/complete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Episode marked as completed"));
  }
}
