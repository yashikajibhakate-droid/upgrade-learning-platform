package com.example.app.repository;

import com.example.app.dto.SeriesRatingSummaryDto;
import com.example.app.model.SeriesReview;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SeriesReviewRepository extends JpaRepository<SeriesReview, UUID> {
  List<SeriesReview> findBySeriesIdAndDeletedFalseOrderByCreatedAtDesc(UUID seriesId);

  List<SeriesReview> findBySeriesIdAndDeletedFalseOrderByCreatedAtAsc(UUID seriesId);

  List<SeriesReview> findBySeriesIdAndDeletedFalse(UUID seriesId);

  Optional<SeriesReview> findByUserEmailAndSeriesId(String userEmail, UUID seriesId);

  boolean existsByUserEmailAndSeriesId(String userEmail, UUID seriesId);

  @org.springframework.data.jpa.repository.Query("SELECT new com.example.app.dto.SeriesRatingSummaryDto(AVG(r.rating), COUNT(r)) "
      +
      "FROM SeriesReview r WHERE r.seriesId = :seriesId AND r.deleted = false")
  SeriesRatingSummaryDto findRatingSummaryBySeriesId(@Param("seriesId") UUID seriesId);
}
