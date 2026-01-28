package com.example.app.repository;

import com.example.app.model.Episode;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, UUID> {
  List<Episode> findBySeriesIdOrderBySequenceNumberAsc(UUID seriesId);
}
