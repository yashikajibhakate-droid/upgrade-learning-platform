package com.example.app.repository;

import com.example.app.model.Session;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

  @Query("SELECT s FROM Session s JOIN FETCH s.user WHERE s.tokenHash = :tokenHash")
  Optional<Session> findByTokenHash(@Param("tokenHash") String tokenHash);

  void deleteByTokenHash(String tokenHash);
}
