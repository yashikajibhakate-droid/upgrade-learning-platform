package com.example.app.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MCQResponse {
  private UUID id;
  private String question;
  private List<MCQOptionDTO> options = new ArrayList<>();
  private String refresherVideoUrl;

  public MCQResponse() {}

  public MCQResponse(
      UUID id, String question, List<MCQOptionDTO> options, String refresherVideoUrl) {
    this.id = id;
    this.question = question;
    this.options = options;
    this.refresherVideoUrl = refresherVideoUrl;
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public List<MCQOptionDTO> getOptions() {
    return options;
  }

  public void setOptions(List<MCQOptionDTO> options) {
    this.options = options;
  }

  public String getRefresherVideoUrl() {
    return refresherVideoUrl;
  }

  public void setRefresherVideoUrl(String refresherVideoUrl) {
    this.refresherVideoUrl = refresherVideoUrl;
  }
}
