package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.example.app.dto.SeriesRankingResponse;
import com.example.app.model.SeriesReview;
import com.example.app.repository.SeriesReviewRepository;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RankingServiceTest {

    @Mock
    private SeriesReviewRepository seriesReviewRepository;

    private RankingService rankingService;
    private final double verifiedWeight = 1.5;
    private final double nonVerifiedWeight = 1.0;

    @BeforeEach
    void setUp() {
        rankingService = new RankingService(
                seriesReviewRepository, verifiedWeight, nonVerifiedWeight);
    }

    @Test
    void calculateScore_VerifiedReviewsWeighHigher() {
        UUID seriesId = UUID.randomUUID();

        SeriesReview verified = new SeriesReview("v@test.com", seriesId, 5, "Great", 90.0, true);
        SeriesReview nonVerified = new SeriesReview("n@test.com", seriesId, 3, "OK", 30.0, false);

        when(seriesReviewRepository.findBySeriesIdAndDeletedFalse(seriesId))
                .thenReturn(List.of(verified, nonVerified));

        SeriesRankingResponse result = rankingService.calculateWeightedRankingScore(seriesId);

        // weighted score = (5*1.5 + 3*1.0) / (1.5 + 1.0) = 10.5 / 2.5 = 4.2
        assertEquals(seriesId, result.seriesId());
        assertEquals(4.2, result.weightedScore(), 0.001);
        assertEquals(2, result.totalReviews());
        assertEquals(1, result.verifiedReviews());
    }

    @Test
    void calculateScore_OnlyNonVerifiedReviews_StillRanked() {
        UUID seriesId = UUID.randomUUID();

        SeriesReview r1 = new SeriesReview("a@test.com", seriesId, 4, "Good", 50.0, false);
        SeriesReview r2 = new SeriesReview("b@test.com", seriesId, 2, "Meh", 20.0, false);

        when(seriesReviewRepository.findBySeriesIdAndDeletedFalse(seriesId))
                .thenReturn(List.of(r1, r2));

        SeriesRankingResponse result = rankingService.calculateWeightedRankingScore(seriesId);

        // weighted score = (4*1.0 + 2*1.0) / (1.0 + 1.0) = 6.0 / 2.0 = 3.0
        assertEquals(3.0, result.weightedScore(), 0.001);
        assertEquals(2, result.totalReviews());
        assertEquals(0, result.verifiedReviews());
    }

    @Test
    void calculateScore_OnlyVerifiedReviews() {
        UUID seriesId = UUID.randomUUID();

        SeriesReview r1 = new SeriesReview("a@test.com", seriesId, 5, "Great", 90.0, true);
        SeriesReview r2 = new SeriesReview("b@test.com", seriesId, 4, "Nice", 85.0, true);

        when(seriesReviewRepository.findBySeriesIdAndDeletedFalse(seriesId))
                .thenReturn(List.of(r1, r2));

        SeriesRankingResponse result = rankingService.calculateWeightedRankingScore(seriesId);

        // weighted score = (5*1.5 + 4*1.5) / (1.5 + 1.5) = 13.5 / 3.0 = 4.5
        assertEquals(4.5, result.weightedScore(), 0.001);
        assertEquals(2, result.totalReviews());
        assertEquals(2, result.verifiedReviews());
    }

    @Test
    void calculateScore_NoReviews_ReturnsZeroScore() {
        UUID seriesId = UUID.randomUUID();

        when(seriesReviewRepository.findBySeriesIdAndDeletedFalse(seriesId))
                .thenReturn(Collections.emptyList());

        SeriesRankingResponse result = rankingService.calculateWeightedRankingScore(seriesId);

        assertEquals(0.0, result.weightedScore(), 0.001);
        assertEquals(0, result.totalReviews());
        assertEquals(0, result.verifiedReviews());
    }

    @Test
    void calculateScore_DifferentWeights_AreRespected() {
        // Use custom weights: verified=2.0, non-verified=0.5
        RankingService customService = new RankingService(
                seriesReviewRepository, 2.0, 0.5);

        UUID seriesId = UUID.randomUUID();

        SeriesReview verified = new SeriesReview("v@test.com", seriesId, 5, "Great", 90.0, true);
        SeriesReview nonVerified = new SeriesReview("n@test.com", seriesId, 3, "OK", 30.0, false);

        when(seriesReviewRepository.findBySeriesIdAndDeletedFalse(seriesId))
                .thenReturn(List.of(verified, nonVerified));

        SeriesRankingResponse result = customService.calculateWeightedRankingScore(seriesId);

        // weighted score = (5*2.0 + 3*0.5) / (2.0 + 0.5) = 11.5 / 2.5 = 4.6
        assertEquals(4.6, result.weightedScore(), 0.001);
    }
}
