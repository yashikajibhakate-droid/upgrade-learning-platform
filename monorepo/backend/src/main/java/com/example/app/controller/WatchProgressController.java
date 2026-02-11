package com.example.app.controller;

import com.example.app.dto.ContinueWatchingResponse;
import com.example.app.service.WatchProgressService;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/watch-progress")
public class WatchProgressController {

  private final WatchProgressService watchProgressService;

  public WatchProgressController(WatchProgressService watchProgressService) {
    this.watchProgressService = watchProgressService;
  }

  @GetMapping("/continue")
  public ResponseEntity<ContinueWatchingResponse> getContinueWatching(@RequestParam String email) {
    Optional<ContinueWatchingResponse> response = watchProgressService.getContinueWatching(email);
    return response.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
  }

  /**
   * Save watch progress for an episode.
   * 
   * @param request The progress data (email, episodeId, progressSeconds)
   * @return Success message
   * @throws com.example.app.exception.ResourceNotFoundException if episode does
   *                                                             not exist
   *                                                             (returns 404)
   * @throws IllegalArgumentException                            if required
   *                                                             fields are
   *                                                             missing (handled
   *                                                             by
   *                                                             Spring/Jackson)
   */
  @PostMapping("/save")
  public ResponseEntity<Map<String, String>> saveProgress(
      @RequestBody SaveProgressRequest request) {
    watchProgressService.saveProgress(
        request.getEmail(), request.getEpisodeId(), request.getProgressSeconds());
    return ResponseEntity.ok(Map.of("message", "Progress saved successfully"));
  }

  @PostMapping("/complete")
  public ResponseEntity<Map<String, String>> markCompleted(
      @RequestBody CompleteEpisodeRequest request) {
    watchProgressService.markCompleted(request.getEmail(), request.getEpisodeId());
    return ResponseEntity.ok(Map.of("message", "Episode marked as completed"));
  }

  @GetMapping("/is-completed")
  public ResponseEntity<Map<String, Boolean>> isEpisodeCompleted(
      @RequestParam String email, @RequestParam UUID episodeId) {
    boolean isCompleted = watchProgressService.isEpisodeCompleted(email, episodeId);
    return ResponseEntity.ok(Map.of("isCompleted", isCompleted));
  }

  static class SaveProgressRequest {
    private String email;
    private UUID episodeId;
    private Integer progressSeconds;

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public UUID getEpisodeId() {
      return episodeId;
    }

    public void setEpisodeId(UUID episodeId) {
      this.episodeId = episodeId;
    }

    public Integer getProgressSeconds() {
      return progressSeconds;
    }

    public void setProgressSeconds(Integer progressSeconds) {
      this.progressSeconds = progressSeconds;
    }
  }

  static class CompleteEpisodeRequest {
    private String email;
    private UUID episodeId;

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public UUID getEpisodeId() {
      return episodeId;
    }

    public void setEpisodeId(UUID episodeId) {
      this.episodeId = episodeId;
    }
  }
}
