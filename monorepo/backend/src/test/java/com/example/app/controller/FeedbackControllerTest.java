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

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private FeedbackService feedbackService;

        // Mocking dependencies required by WebConfig/AuthInterceptor
        @MockBean
        private com.example.app.service.UserService userService;

        @MockBean
        private com.example.app.config.AuthInterceptor authInterceptor;

        @Autowired
        private ObjectMapper objectMapper;

        private String testEmail;
        private UUID testEpisodeId;

        @BeforeEach
        void setUp() throws Exception {
                when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
                testEmail = "test@example.com";
                testEpisodeId = UUID.randomUUID();
        }

        @Test
        void saveFeedback_ValidRequest_ShouldReturnOk() throws Exception {
                // Arrange
                FeedbackController.SaveFeedbackRequest request = new FeedbackController.SaveFeedbackRequest();
                request.setEmail(testEmail);
                request.setEpisodeId(testEpisodeId);
                request.setIsHelpful(true);

                doNothing().when(feedbackService).saveFeedback(testEmail, testEpisodeId, true);

                // Act & Assert
                mockMvc
                                .perform(
                                                post("/api/feedback/save")
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Feedback saved successfully"));

                verify(feedbackService, times(1)).saveFeedback(testEmail, testEpisodeId, true);
        }

        @Test
        void saveFeedback_NotHelpfulFeedback_ShouldReturnOk() throws Exception {
                // Arrange
                FeedbackController.SaveFeedbackRequest request = new FeedbackController.SaveFeedbackRequest();
                request.setEmail(testEmail);
                request.setEpisodeId(testEpisodeId);
                request.setIsHelpful(false);

                doNothing().when(feedbackService).saveFeedback(testEmail, testEpisodeId, false);

                // Act & Assert
                mockMvc
                                .perform(
                                                post("/api/feedback/save")
                                                                .contentType(MediaType.APPLICATION_JSON)
                                                                .content(objectMapper.writeValueAsString(request)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.message").value("Feedback saved successfully"));

                verify(feedbackService, times(1)).saveFeedback(testEmail, testEpisodeId, false);
        }

        @Test
        void feedbackExists_FeedbackExists_ShouldReturnTrue() throws Exception {
                // Arrange
                when(feedbackService.feedbackExists(testEmail, testEpisodeId)).thenReturn(true);

                // Act & Assert
                mockMvc
                                .perform(
                                                get("/api/feedback/exists")
                                                                .param("email", testEmail)
                                                                .param("episodeId", testEpisodeId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.exists").value(true));

                verify(feedbackService, times(1)).feedbackExists(testEmail, testEpisodeId);
        }

        @Test
        void feedbackExists_FeedbackDoesNotExist_ShouldReturnFalse() throws Exception {
                // Arrange
                when(feedbackService.feedbackExists(testEmail, testEpisodeId)).thenReturn(false);

                // Act & Assert
                mockMvc
                                .perform(
                                                get("/api/feedback/exists")
                                                                .param("email", testEmail)
                                                                .param("episodeId", testEpisodeId.toString()))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.exists").value(false));

                verify(feedbackService, times(1)).feedbackExists(testEmail, testEpisodeId);
        }
}
