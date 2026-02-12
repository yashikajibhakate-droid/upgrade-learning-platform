package com.example.app.controller;

import com.example.app.dto.IngestRequest;
import com.example.app.service.IngestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<Void> ingestContent(@Valid @RequestBody IngestRequest request) {
        ingestionService.ingestContent(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
