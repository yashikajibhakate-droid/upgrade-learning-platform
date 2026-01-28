package com.example.app.controller;

import com.example.app.dto.RecommendationResponse;
import com.example.app.model.Series;
import com.example.app.service.SeriesService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/series")
public class SeriesController {

  @Autowired private SeriesService seriesService;

  @GetMapping("/recommendations")
  public ResponseEntity<RecommendationResponse> getRecommendations(@RequestParam String email) {
    if (email == null || email.isEmpty()) {
      return ResponseEntity.badRequest().build();
    }
    return ResponseEntity.ok(seriesService.getRecommendations(email));
  }

  @GetMapping("/{id}")
  public ResponseEntity<Series> getSeriesById(@PathVariable UUID id) {
    try {
      return ResponseEntity.ok(seriesService.getSeriesById(id));
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }
}
