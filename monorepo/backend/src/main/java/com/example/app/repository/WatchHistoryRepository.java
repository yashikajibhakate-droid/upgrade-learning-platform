package com.example.app.repository;

import com.example.app.model.WatchHistory;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, UUID> {
  List<WatchHistory> findByUserEmail(String userEmail);

  List<WatchHistory> findByUserEmailAndIsCompletedTrue(String userEmail);

  Optional<WatchHistory> findTop1ByUserEmailAndIsCompletedFalseOrderByLastWatchedAtDesc(
      String userEmail);

  Optional<WatchHistory> findByUserEmailAndEpisodeId(String userEmail, UUID episodeId);
}
