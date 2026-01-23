package com.example.app.controller;

import com.example.app.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.app.service.RateLimitingService;
import io.github.bucket4j.ConsumptionProbe;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private RateLimitingService rateLimitingService;

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
                    .body(Map.of("message", "Too many requests. Please try again in " + waitForRefill + " seconds."));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");

        if (email == null || email.isEmpty() || otp == null || otp.isEmpty()) {
            return ResponseEntity.badRequest().body("Email and OTP are required");
        }

        boolean isValid = authService.verifyOtpAndLogin(email, otp);

        if (isValid) {
            return ResponseEntity.ok().body(Map.of("message", "Login successful", "email", email));
        } else {
            return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired OTP"));
        }
    }
}
