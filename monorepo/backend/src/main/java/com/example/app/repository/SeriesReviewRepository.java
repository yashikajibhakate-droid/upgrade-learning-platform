package com.example.app.repository;

import com.example.app.model.SeriesReview;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeriesReviewRepository extends JpaRepository<SeriesReview, UUID> {
  List<SeriesReview> findBySeriesIdOrderByCreatedAtDesc(UUID seriesId);

  Optional<SeriesReview> findByUserEmailAndSeriesId(String userEmail, UUID seriesId);

  boolean existsByUserEmailAndSeriesId(String userEmail, UUID seriesId);
}
