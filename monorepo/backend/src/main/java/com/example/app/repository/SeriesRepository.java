package com.example.app.repository;

import com.example.app.model.Series;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeriesRepository extends JpaRepository<Series, UUID> {
  List<Series> findByCategoryIn(Set<String> categories);

  List<Series> findByCategoryNotIn(Set<String> categories);
}
