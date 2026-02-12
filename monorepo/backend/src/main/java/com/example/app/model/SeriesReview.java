package com.example.app.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "series_reviews", uniqueConstraints = {
    @UniqueConstraint(name = "uk_series_review_user_series", columnNames = { "user_email", "series_id" })
})
public class SeriesReview {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(name = "user_email", nullable = false)
  @NotBlank(message = "User email is required")
  @JsonIgnore
  private String userEmail;

  @Column(name = "series_id", nullable = false)
  @NotNull(message = "Series ID is required")
  private UUID seriesId;

  @Column(nullable = false)
  @Min(value = 1, message = "Rating must be at least 1")
  @Max(value = 5, message = "Rating must not exceed 5")
  private Integer rating;

  @Column(length = 1000)
  private String comment;

  @Column(nullable = false)
  private Double progressPercentage;

  @Column(nullable = false)
  private boolean isVerified;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public SeriesReview() {
  }

  public SeriesReview(
      String userEmail,
      UUID seriesId,
      Integer rating,
      String comment,
      Double progressPercentage,
      boolean isVerified) {
    this.userEmail = userEmail;
    this.seriesId = seriesId;
    this.rating = rating;
    this.comment = comment;
    this.progressPercentage = progressPercentage;
    this.isVerified = isVerified;
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

  public UUID getSeriesId() {
    return seriesId;
  }

  public void setSeriesId(UUID seriesId) {
    this.seriesId = seriesId;
  }

  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Double getProgressPercentage() {
    return progressPercentage;
  }

  public void setProgressPercentage(Double progressPercentage) {
    this.progressPercentage = progressPercentage;
  }

  public boolean isVerified() {
    return isVerified;
  }

  public void setVerified(boolean verified) {
    isVerified = verified;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getMaskedUserEmail() {
    if (userEmail == null || !userEmail.contains("@")) {
      return "a***@domain.com";
    }
    int atIndex = userEmail.indexOf("@");
    String username = userEmail.substring(0, atIndex);
    String domain = userEmail.substring(atIndex);

    if (username.length() <= 1) {
      return username + "***" + domain;
    }
    return username.charAt(0) + "***" + domain;
  }
}
