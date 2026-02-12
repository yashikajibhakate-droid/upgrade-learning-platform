package com.example.app.service;

import com.example.app.model.Otp;
import com.example.app.model.User;
import com.example.app.repository.OtpRepository;
import com.example.app.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private OtpRepository otpRepository;

  @Autowired
  private com.example.app.repository.SessionRepository sessionRepository;

  @Autowired
  private EmailService emailService;

  public void generateAndSendOtp(String email) {
    // Generate 6-digit OTP
    String otpCode = String.format("%06d", new Random().nextInt(999999));
    LocalDateTime expiry = LocalDateTime.now().plusMinutes(5); // 5 min expiry

    // Save OTP
    Otp otp = new Otp(email, Otp.hash(otpCode), expiry);
    otpRepository.save(otp);

    // Send Email
    emailService.sendOtpEmail(email, otpCode);
  }

  @Transactional
  public Optional<User> verifyOtpAndLogin(String email, String otpCode) {
    // 1. Find latest OTP for email
    Optional<Otp> otpOpt = otpRepository.findTopByEmailOrderByExpiryTimeDesc(email);

    if (otpOpt.isEmpty()) {
      return Optional.empty(); // No OTP found
    }

    Otp otp = otpOpt.get();

    // 2. Validate Code & Expiry
    if (!otp.verifyOtp(otpCode) || otp.isExpired()) {
      return Optional.empty();
    }

    // 3. OTP Valid -> Create User if doesn't exist
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) {
      user = new User(email);
      user = userRepository.save(user);
    }

    // 4. Cleanup (In real app, maybe mark used instead of delete)
    otpRepository.deleteByEmail(email);

    return Optional.of(user);
  }

  public String createSession(User user) {
    String rawToken = java.util.UUID.randomUUID().toString();
    String tokenHash = com.example.app.model.Session.hashToken(rawToken);

    com.example.app.model.Session session = new com.example.app.model.Session(user, tokenHash);
    // Explicitly use the instance variable here (though locally defined in previous
    // code, assume instance var)
    // The previous code had a local variable shadowing the field:
    // com.example.app.repository.SessionRepository sessionRepository =
    // this.sessionRepository;
    // I will simplify to use the injected instance directly.
    sessionRepository.save(session);
    return rawToken;
  }

  @Transactional
  public void logout(String token) {
    String tokenHash = com.example.app.model.Session.hashToken(token);
    sessionRepository.deleteByTokenHash(tokenHash);
  }

  @Transactional
  public String generateMagicLinkToken(String email) {
    // 1. Generate random token
    String rawToken = java.util.UUID.randomUUID().toString();
    String tokenHash = Otp.hash(rawToken);

    // 2. Create "OTP" record (reusing OTP table for magic link token)
    // Expiry 1 hour
    LocalDateTime expiry = LocalDateTime.now().plusHours(1);
    Otp otp = new Otp(email, tokenHash, expiry);
    otp = otpRepository.save(otp);

    // 3. Return combined token: Base64(id + ":" + rawToken)
    String combined = otp.getId() + ":" + rawToken;
    return java.util.Base64.getEncoder().encodeToString(combined.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }

  @Transactional
  public Optional<String> verifyMagicToken(String combinedToken) {
    try {
      // 1. Decode
      byte[] decodedBytes = java.util.Base64.getDecoder().decode(combinedToken);
      String decoded = new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8);
      String[] parts = decoded.split(":");
      if (parts.length != 2)
        return Optional.empty();

      java.util.UUID id = java.util.UUID.fromString(parts[0]);
      String rawToken = parts[1];

      // 2. Find OTP record
      Optional<Otp> otpOpt = otpRepository.findById(id);
      if (otpOpt.isEmpty())
        return Optional.empty();

      Otp otp = otpOpt.get();

      // 3. Verify
      if (!otp.verifyOtp(rawToken) || otp.isExpired()) {
        return Optional.empty();
      }

      // 4. Login (Create/Get User & Session)
      User user = userRepository.findByEmail(otp.getEmail()).orElse(null);
      if (user == null) {
        user = new User(otp.getEmail());
        user = userRepository.save(user);
      }

      // 5. Cleanup (Single use)
      otpRepository.delete(otp);

      // 6. Create Session
      return Optional.of(createSession(user));

    } catch (Exception e) {
      return Optional.empty();
    }
  }

  @Transactional(readOnly = true)
  public Optional<com.example.app.model.Session> getSession(String token) {
    String tokenHash = com.example.app.model.Session.hashToken(token);
    return sessionRepository.findByTokenHash(tokenHash);
  }
}
