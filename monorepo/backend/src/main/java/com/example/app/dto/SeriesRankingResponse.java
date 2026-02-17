package com.example.app.dto;

import java.util.UUID;

public record SeriesRankingResponse(
        UUID seriesId,
        double weightedScore,
        int totalReviews,
        int verifiedReviews) {
}
