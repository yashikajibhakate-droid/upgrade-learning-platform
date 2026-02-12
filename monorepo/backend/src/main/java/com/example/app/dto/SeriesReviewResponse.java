package com.example.app.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record SeriesReviewResponse(
        UUID id,
        String reviewerName,
        UUID seriesId,
        Integer rating,
        String comment,
        Double progressPercentage,
        boolean isVerified,
        LocalDateTime createdAt) {
}
