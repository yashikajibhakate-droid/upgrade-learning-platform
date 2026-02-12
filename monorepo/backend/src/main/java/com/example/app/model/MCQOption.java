package com.example.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

@Entity
@Table(name = "mcq_option")
public class MCQOption {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "mcq_id", nullable = false)
  @NotNull(message = "MCQ is required")
  @com.fasterxml.jackson.annotation.JsonIgnore
  private MCQ mcq;

  @Column(nullable = false, length = 500)
  @NotNull(message = "Option text is required")
  @Size(min = 1, max = 500, message = "Option text must be between 1 and 500 characters")
  private String optionText;

  @Column(nullable = false)
  @NotNull(message = "isCorrect flag is required")
  private Boolean isCorrect;

  @Column(nullable = false)
  @NotNull(message = "Sequence number is required")
  private Integer sequenceNumber;

  public MCQOption() {}

  public MCQOption(MCQ mcq, String optionText, Boolean isCorrect, Integer sequenceNumber) {
    this.mcq = mcq;
    this.optionText = optionText;
    this.isCorrect = isCorrect;
    this.sequenceNumber = sequenceNumber;
  }

  public UUID getId() {
    return id;
  }

  public MCQ getMcq() {
    return mcq;
  }

  public void setMcq(MCQ mcq) {
    this.mcq = mcq;
  }

  public String getOptionText() {
    return optionText;
  }

  public void setOptionText(String optionText) {
    this.optionText = optionText;
  }

  public Boolean getIsCorrect() {
    return isCorrect;
  }

  public void setIsCorrect(Boolean isCorrect) {
    this.isCorrect = isCorrect;
  }

  public Integer getSequenceNumber() {
    return sequenceNumber;
  }

  public void setSequenceNumber(Integer sequenceNumber) {
    this.sequenceNumber = sequenceNumber;
  }
}
