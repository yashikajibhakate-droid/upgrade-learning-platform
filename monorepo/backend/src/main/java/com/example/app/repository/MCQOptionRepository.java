package com.example.app.repository;

import com.example.app.model.MCQOption;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MCQOptionRepository extends JpaRepository<MCQOption, UUID> {

  @Query("SELECT o FROM MCQOption o WHERE o.mcq.id = :mcqId ORDER BY o.sequenceNumber ASC")
  List<MCQOption> findByMcqId(@Param("mcqId") UUID mcqId);
}
