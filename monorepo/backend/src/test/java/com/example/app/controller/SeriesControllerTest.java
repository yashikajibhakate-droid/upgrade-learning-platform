package com.example.app.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import com.example.app.model.Series;
import com.example.app.model.Episode;
import com.example.app.service.SeriesService;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SeriesController.class)
class SeriesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SeriesService seriesService;

    // Mocking dependencies required by WebConfig/AuthInterceptor
    @MockBean
    private com.example.app.service.UserService userService;
    @MockBean
    private com.example.app.config.AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(authInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void testGetEpisodes_Success() throws Exception {
        UUID seriesId = UUID.randomUUID();
        Episode episode = new Episode();
        episode.setTitle("Test Episode");
        episode.setSequenceNumber(1);

        when(seriesService.getEpisodesForSeries(seriesId)).thenReturn(List.of(episode));

        mockMvc.perform(get("/api/series/" + seriesId + "/episodes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[{\"title\":\"Test Episode\",\"sequenceNumber\":1}]"));
    }

    @Test
    void testGetEpisodes_Empty() throws Exception {
        UUID seriesId = UUID.randomUUID();
        when(seriesService.getEpisodesForSeries(seriesId)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/series/" + seriesId + "/episodes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
