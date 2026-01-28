package com.example.app.controller;

import com.example.app.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void testGetInterests_ReturnsList() throws Exception {
        List<String> interests = List.of("Tech", "Art");
        when(userService.getAvailableInterests()).thenReturn(interests);

        mockMvc.perform(get("/api/users/interests"))
                .andExpect(status().isOk())
                .andExpect(content().json("[\"Tech\", \"Art\"]"));
    }

    @Test
    void testSavePreferences_Success() throws Exception {
        String jsonBody = "{\"email\": \"test@example.com\", \"interests\": [\"Tech\", \"Design\"]}";

        mockMvc.perform(post("/api/users/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(content().string("Interests updated successfully"));

        verify(userService).updateInterests(eq("test@example.com"), any());
    }

    @Test
    void testSavePreferences_MissingEmail_ReturnsBadRequest() throws Exception {
        String jsonBody = "{\"interests\": [\"Tech\"]}";

        mockMvc.perform(post("/api/users/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email is required"));
    }

    @Test
    void testSavePreferences_EmptyInterests_ReturnsBadRequest() throws Exception {
        String jsonBody = "{\"email\": \"test@example.com\", \"interests\": []}";

        mockMvc.perform(post("/api/users/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("At least one interest is required"));
    }

    @Test
    void testSavePreferences_UserNotFound_ReturnsBadRequest() throws Exception {
        String jsonBody = "{\"email\": \"unknown@example.com\", \"interests\": [\"Tech\"]}";

        doThrow(new IllegalArgumentException("User not found"))
                .when(userService).updateInterests(any(), any());

        mockMvc.perform(post("/api/users/preferences")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }
}
