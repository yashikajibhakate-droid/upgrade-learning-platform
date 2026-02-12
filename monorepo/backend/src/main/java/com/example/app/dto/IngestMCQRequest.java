package com.example.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class IngestMCQRequest {

    @NotBlank(message = "Question is required")
    private String question;

    private String refresherVideoUrl;

    @Valid
    @NotEmpty(message = "Options are required")
    private List<IngestMCQOptionRequest> options;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getRefresherVideoUrl() {
        return refresherVideoUrl;
    }

    public void setRefresherVideoUrl(String refresherVideoUrl) {
        this.refresherVideoUrl = refresherVideoUrl;
    }

    public List<IngestMCQOptionRequest> getOptions() {
        return options;
    }

    public void setOptions(List<IngestMCQOptionRequest> options) {
        this.options = options;
    }
}
