package com.example.app.controller;

import com.example.app.model.User;
import com.example.app.service.AuthService;
import com.example.app.service.RateLimitingService;
import io.github.bucket4j.ConsumptionProbe;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  @Autowired private AuthService authService;

  @Autowired private RateLimitingService rateLimitingService;

  @PostMapping("/generate-otp")
  public ResponseEntity<?> generateOtp(@RequestBody Map<String, String> payload) {
    String email = payload.get("email");
    if (email == null || email.isEmpty()) {
      return ResponseEntity.badRequest().body("Email is required");
    }

    ConsumptionProbe probe = rateLimitingService.resolveBucket(email);
    if (probe.isConsumed()) {
      authService.generateAndSendOtp(email);
      return ResponseEntity.ok().body(Map.of("message", "OTP sent successfully"));
    } else {
      long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
      return ResponseEntity.status(429)
          .header("Retry-After", String.valueOf(waitForRefill))
          .body(
              Map.of(
                  "message",
                  "Too many requests. Please try again in " + waitForRefill + " seconds."));
    }
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
    String email = payload.get("email");
    String otp = payload.get("otp");

    if (email == null || email.isEmpty() || otp == null || otp.isEmpty()) {
      return ResponseEntity.badRequest().body("Email and OTP are required");
    }

    Optional<User> userOpt = authService.verifyOtpAndLogin(email, otp);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      boolean hasInterests = user.getInterests() != null && !user.getInterests().isEmpty();
      String token = authService.createSession(user);
      return ResponseEntity.ok()
          .body(
              Map.of(
                  "message", "Login successful",
                  "email", email,
                  "token", token,
                  "hasInterests", hasInterests));
    } else {
      return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired OTP"));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(401)
          .body(Map.of("error", "Invalid or missing Authorization header"));
    }
    String token = authHeader.substring(7);
    if (token.isEmpty()) {
      return ResponseEntity.status(401)
          .body(Map.of("error", "Invalid or missing Authorization header"));
    }
    authService.logout(token);
    return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
  }

  @GetMapping("/me")
  public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
    }
    String token = authHeader.substring(7);
    Optional<com.example.app.model.Session> session = authService.getSession(token);
    if (session.isPresent()) {
      User user = session.get().getUser();
      return ResponseEntity.ok(
          Map.of(
              "email",
              user.getEmail(),
              "hasInterests",
              user.getInterests() != null && !user.getInterests().isEmpty()));
    }
    return ResponseEntity.status(401).body(Map.of("error", "Invalid token"));
  }

  @PostMapping("/magic-login")
  public ResponseEntity<?> magicLogin(@RequestBody Map<String, String> payload) {
    String token = payload.get("token");
    if (token == null || token.isEmpty()) {
      return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
    }

    try {
      Optional<String> authTokenOpt = authService.verifyMagicToken(token);
      if (authTokenOpt.isPresent()) {
        String authToken = authTokenOpt.get();
        // Get user details for response
        Optional<com.example.app.model.Session> session = authService.getSession(authToken);
        if (session.isPresent()) {
          User user = session.get().getUser();
          boolean hasInterests = user.getInterests() != null && !user.getInterests().isEmpty();
          return ResponseEntity.ok(
              Map.of(
                  "message",
                  "Login successful",
                  "email",
                  user.getEmail(),
                  "token",
                  authToken,
                  "hasInterests",
                  hasInterests));
        }
      }
      return ResponseEntity.status(401).body(Map.of("error", "Invalid or expired token"));
    } catch (Exception e) {
      return ResponseEntity.status(401).body(Map.of("error", "Invalid token format"));
    }
  }
}
