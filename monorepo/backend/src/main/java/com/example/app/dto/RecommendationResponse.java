package com.example.app.dto;

import com.example.app.model.Series;
import java.util.List;

public class RecommendationResponse {
  private List<Series> recommended;
  private List<Series> others;

  public RecommendationResponse(List<Series> recommended, List<Series> others) {
    this.recommended = recommended;
    this.others = others;
  }

  public List<Series> getRecommended() {
    return recommended;
  }

  public void setRecommended(List<Series> recommended) {
    this.recommended = recommended;
  }

  public List<Series> getOthers() {
    return others;
  }

  public void setOthers(List<Series> others) {
    this.others = others;
  }
}
