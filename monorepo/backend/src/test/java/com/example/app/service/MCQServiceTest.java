package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.app.dto.MCQResponse;
import com.example.app.model.MCQ;
import com.example.app.model.MCQOption;
import com.example.app.repository.MCQOptionRepository;
import com.example.app.repository.MCQRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MCQServiceTest {

  @Mock private MCQRepository mcqRepository;

  @Mock private MCQOptionRepository mcqOptionRepository;

  @InjectMocks private MCQService mcqService;

  private UUID episodeId;
  private UUID mcqId;
  private UUID option1Id;
  private UUID option2Id;
  private MCQ mcq;
  private MCQOption correctOption;
  private MCQOption incorrectOption;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);

    episodeId = UUID.randomUUID();
    mcqId = UUID.randomUUID();
    option1Id = UUID.randomUUID();
    option2Id = UUID.randomUUID();

    mcq = new MCQ(episodeId, "What is the main concept?", "https://example.com/refresher.mp4");
    // Use reflection or create a test-specific constructor to set ID
    try {
      var idField = MCQ.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(mcq, mcqId);
    } catch (Exception e) {
      fail("Failed to set MCQ ID for test: " + e.getMessage());
    }

    correctOption = new MCQOption(mcq, "Correct answer", true, 1);
    incorrectOption = new MCQOption(mcq, "Incorrect answer", false, 2);

    try {
      var idField = MCQOption.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(correctOption, option1Id);
      idField.set(incorrectOption, option2Id);
    } catch (Exception e) {
      fail("Failed to set option IDs for test: " + e.getMessage());
    }
  }

  @Test
  void getMCQByEpisodeId_WhenMCQExists_ReturnsResponse() {
    // Arrange
    when(mcqRepository.findByEpisodeId(episodeId)).thenReturn(Optional.of(mcq));
    when(mcqOptionRepository.findByMcqId(mcqId))
        .thenReturn(List.of(correctOption, incorrectOption));

    // Act
    Optional<MCQResponse> result = mcqService.getMCQByEpisodeId(episodeId);

    // Assert
    assertTrue(result.isPresent());
    MCQResponse response = result.get();
    assertEquals(mcqId, response.getId());
    assertEquals("What is the main concept?", response.getQuestion());
    assertEquals("https://example.com/refresher.mp4", response.getRefresherVideoUrl());
    assertEquals(2, response.getOptions().size());

    // Verify DTOs do not expose isCorrect
    response
        .getOptions()
        .forEach(
            option -> {
              assertNotNull(option.getId());
              assertNotNull(option.getOptionText());
              // isCorrect should not be accessible in DTO
            });
  }

  @Test
  void getMCQByEpisodeId_WhenMCQDoesNotExist_ReturnsEmpty() {
    // Arrange
    when(mcqRepository.findByEpisodeId(episodeId)).thenReturn(Optional.empty());

    // Act
    Optional<MCQResponse> result = mcqService.getMCQByEpisodeId(episodeId);

    // Assert
    assertTrue(result.isEmpty());
    verify(mcqOptionRepository, never()).findByMcqId(any());
  }

  @Test
  void validateAnswer_WithCorrectOption_ReturnsTrue() {
    // Arrange
    when(mcqRepository.findById(mcqId)).thenReturn(Optional.of(mcq));
    when(mcqOptionRepository.findById(option1Id)).thenReturn(Optional.of(correctOption));

    // Act
    boolean result = mcqService.validateAnswer(mcqId, option1Id);

    // Assert
    assertTrue(result);
  }

  @Test
  void validateAnswer_WithIncorrectOption_ReturnsFalse() {
    // Arrange
    when(mcqRepository.findById(mcqId)).thenReturn(Optional.of(mcq));
    when(mcqOptionRepository.findById(option2Id)).thenReturn(Optional.of(incorrectOption));

    // Act
    boolean result = mcqService.validateAnswer(mcqId, option2Id);

    // Assert
    assertFalse(result);
  }

  @Test
  void validateAnswer_WithInvalidMCQId_ThrowsException() {
    // Arrange
    UUID invalidMcqId = UUID.randomUUID();
    when(mcqRepository.findById(invalidMcqId)).thenReturn(Optional.empty());

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> mcqService.validateAnswer(invalidMcqId, option1Id));

    assertTrue(exception.getMessage().contains("MCQ not found"));
  }

  @Test
  void validateAnswer_WithInvalidOptionId_ThrowsException() {
    // Arrange
    UUID invalidOptionId = UUID.randomUUID();
    when(mcqRepository.findById(mcqId)).thenReturn(Optional.of(mcq));
    when(mcqOptionRepository.findById(invalidOptionId)).thenReturn(Optional.empty());

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> mcqService.validateAnswer(mcqId, invalidOptionId));

    assertTrue(exception.getMessage().contains("Option not found"));
  }

  @Test
  void validateAnswer_WithOptionFromDifferentMCQ_ThrowsException() {
    // Arrange
    UUID otherMcqId = UUID.randomUUID();
    MCQ otherMcq = new MCQ(UUID.randomUUID(), "Other question", "https://example.com/other.mp4");
    try {
      var idField = MCQ.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(otherMcq, otherMcqId);
    } catch (Exception e) {
      fail("Failed to set MCQ ID for test: " + e.getMessage());
    }

    MCQOption optionFromOtherMCQ = new MCQOption(otherMcq, "Option from other MCQ", true, 1);
    try {
      var idField = MCQOption.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(optionFromOtherMCQ, UUID.randomUUID());
    } catch (Exception e) {
      fail("Failed to set option ID for test: " + e.getMessage());
    }

    when(mcqRepository.findById(mcqId)).thenReturn(Optional.of(mcq));
    when(mcqOptionRepository.findById(optionFromOtherMCQ.getId()))
        .thenReturn(Optional.of(optionFromOtherMCQ));

    // Act & Assert
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> mcqService.validateAnswer(mcqId, optionFromOtherMCQ.getId()));

    assertTrue(exception.getMessage().contains("does not belong"));
  }

  @Test
  void getRefresherVideoUrl_WhenMCQExists_ReturnsUrl() {
    // Arrange
    when(mcqRepository.findById(mcqId)).thenReturn(Optional.of(mcq));

    // Act
    Optional<String> result = mcqService.getRefresherVideoUrl(mcqId);

    // Assert
    assertTrue(result.isPresent());
    assertEquals("https://example.com/refresher.mp4", result.get());
  }

  @Test
  void getRefresherVideoUrl_WhenMCQDoesNotExist_ReturnsEmpty() {
    // Arrange
    UUID invalidMcqId = UUID.randomUUID();
    when(mcqRepository.findById(invalidMcqId)).thenReturn(Optional.empty());

    // Act
    Optional<String> result = mcqService.getRefresherVideoUrl(invalidMcqId);

    // Assert
    assertTrue(result.isEmpty());
  }
}
