package com.example.app.controller;

import com.example.app.model.EpisodeComment;
import com.example.app.service.EpisodeCommentService;
import java.util.List;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

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
            HttpServletRequest request,
            @PathVariable UUID episodeId,
            @jakarta.validation.Valid @RequestBody com.example.app.dto.CommentRequest payload) {

        com.example.app.model.User user = (com.example.app.model.User) request.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }

        EpisodeComment comment = commentService.addComment(episodeId, user.getEmail(), payload.content());
        return ResponseEntity.ok(comment);
    }
}
