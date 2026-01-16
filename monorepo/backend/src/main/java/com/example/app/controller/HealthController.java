package com.example.app.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

  @GetMapping("/health")
  public Map<String, Object> health() {
    Map<String, Object> status = new HashMap<>();
    status.put("status", "UP");
    status.put("timestamp", LocalDateTime.now());
    status.put("message", "Backend is running smoothly!");
    return status;
  }
}
