package com.example.app.repository;

import com.example.app.model.EpisodeComment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpisodeCommentRepository extends JpaRepository<EpisodeComment, UUID> {
    List<EpisodeComment> findByEpisodeIdAndStatusOrderByCreatedAtDesc(
            UUID episodeId, EpisodeComment.Status status);
}
