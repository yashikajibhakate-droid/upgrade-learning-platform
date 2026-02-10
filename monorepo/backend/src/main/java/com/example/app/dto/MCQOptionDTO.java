package com.example.app.dto;

import java.util.UUID;

public class MCQOptionDTO {
    private UUID id;
    private String optionText;

    public MCQOptionDTO() {
    }

    public MCQOptionDTO(UUID id, String optionText) {
        this.id = id;
        this.optionText = optionText;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }
}
