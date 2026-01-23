package com.example.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "otps")
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String otpCodeHash;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    public Otp() {
    }

    public Otp(String email, String otpCodeHash, LocalDateTime expiryTime) {
        this.email = email;
        this.otpCodeHash = otpCodeHash;
        this.expiryTime = expiryTime;
    }

    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public boolean verifyOtp(String candidate) {
        try {
            String[] parts = this.otpCodeHash.split(":");
            if (parts.length != 2)
                return false;
            String salt = parts[0];
            String storedHash = parts[1];

            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String content = salt + candidate;
            byte[] hash = digest.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String candidateHash = java.util.Base64.getEncoder().encodeToString(hash);

            return storedHash.equals(candidateHash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Error verifying OTP", e);
        }
    }

    public static String hash(String otp) {
        try {
            String salt = UUID.randomUUID().toString();
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String content = salt + otp;
            byte[] hash = digest.digest(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            String encodedHash = java.util.Base64.getEncoder().encodeToString(hash);
            return salt + ":" + encodedHash;
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public LocalDateTime getExpiryTime() {
        return expiryTime;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryTime);
    }
}
