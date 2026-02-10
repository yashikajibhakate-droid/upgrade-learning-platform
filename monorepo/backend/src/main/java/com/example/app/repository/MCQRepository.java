package com.example.app.repository;

import com.example.app.model.MCQ;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MCQRepository extends JpaRepository<MCQ, UUID> {
    Optional<MCQ> findByEpisodeId(UUID episodeId);
}
