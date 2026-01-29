package com.example.app.controller;

import com.example.app.service.UserService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(UserController.class);

  @Autowired
  private UserService userService;

  @GetMapping("/interests")
  public ResponseEntity<List<String>> getInterests() {
    return ResponseEntity.ok(userService.getAvailableInterests());
  }

  @PostMapping("/preferences")
  public ResponseEntity<?> savePreferences(@RequestBody Map<String, Object> payload) {
    String email = (String) payload.get("email");

    if (email == null || email.isEmpty()) {
      return ResponseEntity.badRequest().body("Email is required");
    }

    Object interestsObj = payload.get("interests");
    List<String> interestsList = new java.util.ArrayList<>();

    if (interestsObj instanceof List<?>) {
      for (Object item : (List<?>) interestsObj) {
        if (item instanceof String) {
          interestsList.add((String) item);
        } else {
          return ResponseEntity.badRequest().body("Interests must be a list of strings");
        }
      }
    } else {
      return ResponseEntity.badRequest().body("Interests must be a valid list");
    }

    if (interestsList.isEmpty()) {
      return ResponseEntity.badRequest().body("At least one interest is required");
    }

    try {
      userService.updateInterests(email, new java.util.HashSet<>(interestsList));
      return ResponseEntity.ok().body("Interests updated successfully");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (Exception e) {
      logger.error("Error saving preferences", e);
      return ResponseEntity.status(500).body("Error saving preferences");
    }
  }
}
