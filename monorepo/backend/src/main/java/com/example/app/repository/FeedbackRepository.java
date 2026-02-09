package com.example.app.repository;

import com.example.app.model.Feedback;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {

    Optional<Feedback> findByUserEmailAndEpisodeId(String userEmail, UUID episodeId);
}
