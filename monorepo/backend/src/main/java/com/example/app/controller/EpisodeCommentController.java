package com.example.app.controller;

import com.example.app.model.EpisodeComment;
import com.example.app.service.EpisodeCommentService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/episodes/{episodeId}/comments")

public class EpisodeCommentController {

    @Autowired
    private EpisodeCommentService commentService;

    @GetMapping
    public ResponseEntity<List<EpisodeComment>> getComments(@PathVariable UUID episodeId) {
        return ResponseEntity.ok(commentService.getApprovedComments(episodeId));
    }

    @PostMapping
    public ResponseEntity<EpisodeComment> addComment(
            @PathVariable UUID episodeId, @RequestBody Map<String, String> payload) {
        String userEmail = payload.get("userEmail");
        String content = payload.get("content");

        if (userEmail == null || content == null) {
            return ResponseEntity.badRequest().build();
        }

        EpisodeComment comment = commentService.addComment(episodeId, userEmail, content);
        return ResponseEntity.ok(comment);
    }
}
