package com.example.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_interests", joinColumns = @JoinColumn(name = "user_id"))
  @Column(name = "interest")
  private java.util.Set<String> interests = new java.util.HashSet<>();

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "user_interest_weights", joinColumns = @JoinColumn(name = "user_id"))
  @MapKeyColumn(name = "category")
  @Column(name = "weight")
  private java.util.Map<String, Integer> interestWeights = new java.util.HashMap<>();

  public User() {}

  public User(String email) {
    this.email = email;
  }

  public UUID getId() {
    return id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public java.util.Set<String> getInterests() {
    return interests;
  }

  public void setInterests(java.util.Set<String> interests) {
    this.interests = interests;
  }

  public java.util.Map<String, Integer> getInterestWeights() {
    return interestWeights;
  }

  public void setInterestWeights(java.util.Map<String, Integer> interestWeights) {
    this.interestWeights = interestWeights;
  }
}
