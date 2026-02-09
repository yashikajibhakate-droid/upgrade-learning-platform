package com.example.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "feedback",
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_feedback_user_episode",
          columnNames = {"user_email", "episode_id"})
    })
public class Feedback {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(nullable = false, length = 255)
  @Email(message = "Invalid email format")
  @Size(max = 255, message = "Email must not exceed 255 characters")
  private String userEmail;

  @Column(nullable = false)
  private UUID episodeId;

  @Column(nullable = false)
  private Boolean isHelpful;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = true)
  private LocalDateTime updatedAt;

  public Feedback() {}

  public Feedback(String userEmail, UUID episodeId, Boolean isHelpful) {
    this.userEmail = userEmail;
    this.episodeId = episodeId;
    this.isHelpful = isHelpful;
    this.createdAt = LocalDateTime.now();
  }

  public UUID getId() {
    return id;
  }

  public String getUserEmail() {
    return userEmail;
  }

  public void setUserEmail(String userEmail) {
    this.userEmail = userEmail;
  }

  public UUID getEpisodeId() {
    return episodeId;
  }

  public void setEpisodeId(UUID episodeId) {
    this.episodeId = episodeId;
  }

  public Boolean getIsHelpful() {
    return isHelpful;
  }

  public void setIsHelpful(Boolean isHelpful) {
    this.isHelpful = isHelpful;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
