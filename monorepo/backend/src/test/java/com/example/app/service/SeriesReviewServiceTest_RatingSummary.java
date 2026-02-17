package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.app.dto.SeriesRatingSummaryDto;
import com.example.app.repository.SeriesReviewRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeriesReviewServiceTest_RatingSummary {

    @Mock
    private SeriesReviewRepository seriesReviewRepository;
    @Mock
    private WatchProgressService watchProgressService;

    private SeriesReviewService seriesReviewService;

    @BeforeEach
    void setUp() {
        seriesReviewService = new SeriesReviewService(seriesReviewRepository, watchProgressService, 80.0, 24);
    }

    @Test
    void getRatingSummary_ReturnSummaryFromRepository() {
        // Arrange
        UUID seriesId = UUID.randomUUID();
        SeriesRatingSummaryDto expectedSummary = new SeriesRatingSummaryDto(4.5, 10L);

        when(seriesReviewRepository.findRatingSummaryBySeriesId(seriesId)).thenReturn(expectedSummary);

        // Act
        SeriesRatingSummaryDto result = seriesReviewService.getRatingSummary(seriesId);

        // Assert
        assertNotNull(result);
        assertEquals(4.5, result.averageRating());
        assertEquals(10L, result.totalReviews());
        verify(seriesReviewRepository).findRatingSummaryBySeriesId(seriesId);
    }
}
