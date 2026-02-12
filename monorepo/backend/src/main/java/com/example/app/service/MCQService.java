package com.example.app.service;

import com.example.app.dto.MCQOptionDTO;
import com.example.app.dto.MCQResponse;
import com.example.app.model.MCQ;
import com.example.app.model.MCQOption;
import com.example.app.repository.MCQOptionRepository;
import com.example.app.repository.MCQRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MCQService {

  private final MCQRepository mcqRepository;
  private final MCQOptionRepository mcqOptionRepository;

  public MCQService(MCQRepository mcqRepository, MCQOptionRepository mcqOptionRepository) {
    this.mcqRepository = mcqRepository;
    this.mcqOptionRepository = mcqOptionRepository;
  }

  /**
   * Get MCQ for a specific episode. Returns empty if no MCQ exists.
   *
   * @param episodeId The episode ID to find MCQ for
   * @return Optional containing MCQResponse if found, empty otherwise
   */
  public Optional<MCQResponse> getMCQByEpisodeId(UUID episodeId) {
    Optional<MCQ> mcqOptional = mcqRepository.findByEpisodeId(episodeId);

    if (mcqOptional.isEmpty()) {
      return Optional.empty();
    }

    MCQ mcq = mcqOptional.get();

    // Fetch options for this MCQ
    List<MCQOption> options = mcqOptionRepository.findByMcqId(mcq.getId());

    // Transform to DTOs (hiding isCorrect field)
    List<MCQOptionDTO> optionDTOs =
        options.stream()
            .map(option -> new MCQOptionDTO(option.getId(), option.getOptionText()))
            .collect(Collectors.toList());

    MCQResponse response =
        new MCQResponse(mcq.getId(), mcq.getQuestion(), optionDTOs, mcq.getRefresherVideoUrl());

    return Optional.of(response);
  }

  /**
   * Validate if the selected option is correct for the given MCQ.
   *
   * @param mcqId The MCQ ID
   * @param selectedOptionId The selected option ID
   * @return true if correct, false otherwise
   * @throws IllegalArgumentException if MCQ or option not found
   */
  public boolean validateAnswer(UUID mcqId, UUID selectedOptionId) {
    // Verify MCQ exists
    mcqRepository
        .findById(mcqId)
        .orElseThrow(() -> new IllegalArgumentException("MCQ not found with ID: " + mcqId));

    // Verify option exists and belongs to this MCQ
    MCQOption selectedOption =
        mcqOptionRepository
            .findById(selectedOptionId)
            .orElseThrow(
                () ->
                    new IllegalArgumentException("Option not found with ID: " + selectedOptionId));

    // Ensure the option belongs to the MCQ
    if (!selectedOption.getMcq().getId().equals(mcqId)) {
      throw new IllegalArgumentException("Option does not belong to the specified MCQ");
    }

    return selectedOption.getIsCorrect();
  }

  /**
   * Get the refresher video URL for an MCQ if it exists.
   *
   * @param mcqId The MCQ ID
   * @return Optional containing refresher video URL if found
   */
  public Optional<String> getRefresherVideoUrl(UUID mcqId) {
    return mcqRepository.findById(mcqId).map(MCQ::getRefresherVideoUrl);
  }
}
