package com.example.app.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SeriesReviewTest {

  @Test
  void getMaskedUserEmail_ShouldMaskCorrectly() {
    SeriesReview review = new SeriesReview();

    review.setUserEmail("yashika@technogise.com");
    assertEquals("y***@technogise.com", review.getMaskedUserEmail());

    review.setUserEmail("a@b.com");
    assertEquals("a***@b.com", review.getMaskedUserEmail());

    review.setUserEmail("ab@c.com");
    assertEquals("a***@c.com", review.getMaskedUserEmail());

    review.setUserEmail(null);
    assertEquals("a***@domain.com", review.getMaskedUserEmail());

    review.setUserEmail("invalid-email");
    assertEquals("a***@domain.com", review.getMaskedUserEmail());
  }

  @Test
  void userEmail_ShouldBeIgnoredDuringSerialization() throws Exception {
    SeriesReview review = new SeriesReview(
        "test@example.com",
        UUID.randomUUID(),
        5,
        "Great",
        100.0,
        true);

    ObjectMapper mapper = new ObjectMapper();
    // Register modules if needed, but for a simple POJO it should work
    mapper.findAndRegisterModules();

    String json = mapper.writeValueAsString(review);

    // The raw email should not be present in the JSON
    org.junit.jupiter.api.Assertions.assertFalse(json.contains("test@example.com"), "PII leaked in JSON: " + json);
    org.junit.jupiter.api.Assertions.assertFalse(json.contains("userEmail"), "userEmail field should be ignored");
  }

  @Test
  void seriesReviewResponse_ShouldIncludeReviewerName() throws Exception {
    com.example.app.dto.SeriesReviewResponse response = new com.example.app.dto.SeriesReviewResponse(
        UUID.randomUUID(),
        "t***@example.com",
        UUID.randomUUID(),
        5,
        "Great",
        100.0,
        true,
        java.time.LocalDateTime.now(),
        null,
        false,
        true);

    ObjectMapper mapper = new ObjectMapper();
    mapper.findAndRegisterModules();

    String json = mapper.writeValueAsString(response);

    org.junit.jupiter.api.Assertions.assertTrue(json.contains("reviewerName"),
        "reviewerName field should be present in DTO");
    org.junit.jupiter.api.Assertions.assertFalse(json.contains("userEmail"),
        "userEmail field should NOT be present in DTO");
  }
}
