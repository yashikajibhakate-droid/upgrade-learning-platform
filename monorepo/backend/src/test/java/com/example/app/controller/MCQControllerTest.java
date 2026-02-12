package com.example.app.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.app.dto.MCQOptionDTO;
import com.example.app.dto.MCQResponse;
import com.example.app.service.MCQService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MCQController.class)
class MCQControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private MCQService mcqService;

  @MockBean private com.example.app.service.UserService userService;

  @MockBean private com.example.app.config.AuthInterceptor authInterceptor;

  @Autowired private ObjectMapper objectMapper;

  private com.example.app.model.User testUser;
  private UUID episodeId;
  private UUID mcqId;
  private UUID option1Id;
  private UUID option2Id;
  private MCQResponse mcqResponse;

  @BeforeEach
  void setUp() throws Exception {
    testUser = new com.example.app.model.User("test@example.com");
    episodeId = UUID.randomUUID();
    mcqId = UUID.randomUUID();
    option1Id = UUID.randomUUID();
    option2Id = UUID.randomUUID();

    // Mock AuthInterceptor
    when(authInterceptor.preHandle(any(), any(), any()))
        .thenAnswer(
            invocation -> {
              jakarta.servlet.http.HttpServletRequest request = invocation.getArgument(0);
              request.setAttribute("user", testUser);
              return true;
            });

    // Setup mock MCQ response
    MCQOptionDTO option1 = new MCQOptionDTO(option1Id, "Correct answer");
    MCQOptionDTO option2 = new MCQOptionDTO(option2Id, "Incorrect answer");
    mcqResponse =
        new MCQResponse(
            mcqId,
            "What is the main concept?",
            Arrays.asList(option1, option2),
            "https://example.com/refresher.mp4");
  }

  @Test
  void getMCQByEpisodeId_WhenMCQExists_ShouldReturnOk() throws Exception {
    // Arrange
    when(mcqService.getMCQByEpisodeId(episodeId)).thenReturn(Optional.of(mcqResponse));

    // Act & Assert
    mockMvc
        .perform(get("/api/mcq/{episodeId}", episodeId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(mcqId.toString()))
        .andExpect(jsonPath("$.question").value("What is the main concept?"))
        .andExpect(jsonPath("$.refresherVideoUrl").value("https://example.com/refresher.mp4"))
        .andExpect(jsonPath("$.options.length()").value(2))
        .andExpect(jsonPath("$.options[0].id").value(option1Id.toString()))
        .andExpect(jsonPath("$.options[0].optionText").value("Correct answer"));

    verify(mcqService, times(1)).getMCQByEpisodeId(episodeId);
  }

  @Test
  void getMCQByEpisodeId_WhenMCQDoesNotExist_ShouldReturn404() throws Exception {
    // Arrange
    when(mcqService.getMCQByEpisodeId(episodeId)).thenReturn(Optional.empty());

    // Act & Assert
    mockMvc
        .perform(get("/api/mcq/{episodeId}", episodeId))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("No MCQ found for this episode"));

    verify(mcqService, times(1)).getMCQByEpisodeId(episodeId);
  }

  @Test
  void validateAnswer_WithCorrectAnswer_ShouldReturnCorrect() throws Exception {
    // Arrange
    MCQController.ValidateAnswerRequest request = new MCQController.ValidateAnswerRequest();
    request.setMcqId(mcqId);
    request.setSelectedOptionId(option1Id);

    when(mcqService.validateAnswer(mcqId, option1Id)).thenReturn(true);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/mcq/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correct").value(true))
        .andExpect(jsonPath("$.refresherVideoUrl").doesNotExist());

    verify(mcqService, times(1)).validateAnswer(mcqId, option1Id);
    verify(mcqService, never()).getRefresherVideoUrl(any());
  }

  @Test
  void validateAnswer_WithIncorrectAnswer_ShouldReturnIncorrectWithRefresher() throws Exception {
    // Arrange
    MCQController.ValidateAnswerRequest request = new MCQController.ValidateAnswerRequest();
    request.setMcqId(mcqId);
    request.setSelectedOptionId(option2Id);

    when(mcqService.validateAnswer(mcqId, option2Id)).thenReturn(false);
    when(mcqService.getRefresherVideoUrl(mcqId))
        .thenReturn(Optional.of("https://example.com/refresher.mp4"));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/mcq/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correct").value(false))
        .andExpect(jsonPath("$.refresherVideoUrl").value("https://example.com/refresher.mp4"));

    verify(mcqService, times(1)).validateAnswer(mcqId, option2Id);
    verify(mcqService, times(1)).getRefresherVideoUrl(mcqId);
  }

  @Test
  void validateAnswer_WithInvalidMCQId_ShouldReturnBadRequest() throws Exception {
    // Arrange
    UUID invalidMcqId = UUID.randomUUID();
    MCQController.ValidateAnswerRequest request = new MCQController.ValidateAnswerRequest();
    request.setMcqId(invalidMcqId);
    request.setSelectedOptionId(option1Id);

    when(mcqService.validateAnswer(invalidMcqId, option1Id))
        .thenThrow(new IllegalArgumentException("MCQ not found"));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/mcq/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("MCQ not found"));

    verify(mcqService, times(1)).validateAnswer(invalidMcqId, option1Id);
  }

  @Test
  void validateAnswer_NullMcqId_ShouldReturnBadRequest() throws Exception {
    // Arrange
    MCQController.ValidateAnswerRequest request = new MCQController.ValidateAnswerRequest();
    request.setMcqId(null);
    request.setSelectedOptionId(option1Id);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/mcq/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(mcqService, never()).validateAnswer(any(), any());
  }

  @Test
  void validateAnswer_NullSelectedOptionId_ShouldReturnBadRequest() throws Exception {
    // Arrange
    MCQController.ValidateAnswerRequest request = new MCQController.ValidateAnswerRequest();
    request.setMcqId(mcqId);
    request.setSelectedOptionId(null);

    // Act & Assert
    mockMvc
        .perform(
            post("/api/mcq/validate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());

    verify(mcqService, never()).validateAnswer(any(), any());
  }
}
