package com.example.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mcq")
public class MCQ {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "episode_id", nullable = false, unique = true)
    @NotNull(message = "Episode ID is required")
    private UUID episodeId;

    @Column(nullable = false, length = 1000)
    @NotNull(message = "Question is required")
    @Size(min = 5, max = 1000, message = "Question must be between 5 and 1000 characters")
    private String question;

    @Column(length = 500)
    @Size(max = 500, message = "Refresher video URL must not exceed 500 characters")
    private String refresherVideoUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "mcq", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MCQOption> options = new ArrayList<>();

    public MCQ() {
    }

    public MCQ(UUID episodeId, String question, String refresherVideoUrl) {
        this.episodeId = episodeId;
        this.question = question;
        this.refresherVideoUrl = refresherVideoUrl;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(UUID episodeId) {
        this.episodeId = episodeId;
    }

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

    public List<MCQOption> getOptions() {
        return options;
    }

    public void setOptions(List<MCQOption> options) {
        this.options = options;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
