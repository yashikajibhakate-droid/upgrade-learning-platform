package com.example.app.service;

import com.example.app.dto.SeriesRankingResponse;
import com.example.app.model.SeriesReview;
import com.example.app.repository.SeriesReviewRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RankingService {

    private final SeriesReviewRepository seriesReviewRepository;
    private final double verifiedWeight;
    private final double nonVerifiedWeight;

    public RankingService(
            SeriesReviewRepository seriesReviewRepository,
            @Value("${app.reviews.verified-weight:1.5}") double verifiedWeight,
            @Value("${app.reviews.non-verified-weight:1.0}") double nonVerifiedWeight) {
        this.seriesReviewRepository = seriesReviewRepository;
        this.verifiedWeight = verifiedWeight;
        this.nonVerifiedWeight = nonVerifiedWeight;
    }

    @Transactional(readOnly = true)
    public SeriesRankingResponse calculateWeightedRankingScore(UUID seriesId) {
        List<SeriesReview> reviews = seriesReviewRepository.findBySeriesIdAndDeletedFalse(seriesId);

        if (reviews.isEmpty()) {
            return new SeriesRankingResponse(seriesId, 0.0, 0, 0);
        }

        double weightedSum = 0.0;
        double totalWeight = 0.0;
        int verifiedCount = 0;

        for (SeriesReview review : reviews) {
            double weight = review.isVerified() ? verifiedWeight : nonVerifiedWeight;
            weightedSum += review.getRating() * weight;
            totalWeight += weight;
            if (review.isVerified()) {
                verifiedCount++;
            }
        }

        double score = totalWeight > 0 ? weightedSum / totalWeight : 0.0;

        return new SeriesRankingResponse(seriesId, score, reviews.size(), verifiedCount);
    }
}
