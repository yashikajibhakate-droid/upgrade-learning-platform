package com.example.app.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.app.service.FeedbackService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FeedbackController.class)
class FeedbackControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private FeedbackService feedbackService;

  // Mocking dependencies required by WebConfig/AuthInterceptor
  @MockBean private com.example.app.service.UserService userService;

  @MockBean private com.example.app.config.AuthInterceptor authInterceptor;

  @Autowired private ObjectMapper objectMapper;

  private com.example.app.model.User testUser;
  private UUID testEpisodeId;

  @BeforeEach
  void setUp() throws Exception {
    testUser = new com.example.app.model.User("test@example.com");
    testEpisodeId = UUID.randomUUID();

    // Mock AuthInterceptor to set user in request
    when(authInterceptor.preHandle(any(), any(), any()))
        .thenAnswer(
            invocation -> {
              jakarta.servlet.http.HttpServletRequest request = invocation.getArgument(0);
              request.setAttribute("user", testUser);
              return true;
            });
  }

  @Test
  void saveFeedback_ValidRequest_ShouldReturnOk() throws Exception {
    // Arrange
    FeedbackController.SaveFeedbackRequest request = new FeedbackController.SaveFeedbackRequest();
    request.setEpisodeId(testEpisodeId);
    request.setIsHelpful(true);

    doNothing().when(feedbackService).saveFeedback("test@example.com", testEpisodeId, true);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/feedback/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Feedback saved successfully"));

    verify(feedbackService, times(1)).saveFeedback("test@example.com", testEpisodeId, true);
  }

  @Test
  void saveFeedback_NotHelpfulFeedback_ShouldReturnOk() throws Exception {
    // Arrange
    FeedbackController.SaveFeedbackRequest request = new FeedbackController.SaveFeedbackRequest();
    request.setEpisodeId(testEpisodeId);
    request.setIsHelpful(false);

    doNothing().when(feedbackService).saveFeedback("test@example.com", testEpisodeId, false);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/feedback/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Feedback saved successfully"));

    verify(feedbackService, times(1)).saveFeedback("test@example.com", testEpisodeId, false);
  }

  @Test
  void feedbackExists_FeedbackExists_ShouldReturnTrue() throws Exception {
    // Arrange
    FeedbackController.FeedbackExistsRequest request =
        new FeedbackController.FeedbackExistsRequest();
    request.setEpisodeId(testEpisodeId);

    when(feedbackService.feedbackExists("test@example.com", testEpisodeId)).thenReturn(true);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/feedback/exists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.exists").value(true));

    verify(feedbackService, times(1)).feedbackExists("test@example.com", testEpisodeId);
  }

  @Test
  void feedbackExists_FeedbackDoesNotExist_ShouldReturnFalse() throws Exception {
    // Arrange
    FeedbackController.FeedbackExistsRequest request =
        new FeedbackController.FeedbackExistsRequest();
    request.setEpisodeId(testEpisodeId);

    when(feedbackService.feedbackExists("test@example.com", testEpisodeId)).thenReturn(false);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/feedback/exists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.exists").value(false));

    verify(feedbackService, times(1)).feedbackExists("test@example.com", testEpisodeId);
  }

  @Test
  void saveFeedback_NullEpisodeId_ShouldReturnBadRequest() throws Exception {
    // Arrange - episodeId is null
    FeedbackController.SaveFeedbackRequest request = new FeedbackController.SaveFeedbackRequest();
    request.setEpisodeId(null);
    request.setIsHelpful(true);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/feedback/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    // Service should not be called
    verify(feedbackService, never()).saveFeedback(anyString(), any(), any());
  }

  @Test
  void saveFeedback_NullIsHelpful_ShouldReturnBadRequest() throws Exception {
    // Arrange - isHelpful is null
    FeedbackController.SaveFeedbackRequest request = new FeedbackController.SaveFeedbackRequest();
    request.setEpisodeId(testEpisodeId);
    request.setIsHelpful(null);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/feedback/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    // Service should not be called
    verify(feedbackService, never()).saveFeedback(anyString(), any(), any());
  }

  @Test
  void saveFeedback_AllFieldsNull_ShouldReturnBadRequest() throws Exception {
    // Arrange - all fields null
    FeedbackController.SaveFeedbackRequest request = new FeedbackController.SaveFeedbackRequest();

    // Act & Assert
    mockMvc
        .perform(
            post("/api/feedback/save")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    // Service should not be called
    verify(feedbackService, never()).saveFeedback(anyString(), any(), any());
  }
}
