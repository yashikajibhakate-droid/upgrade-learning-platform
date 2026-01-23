package com.example.app.service;

import com.example.app.model.Otp;
import com.example.app.model.User;
import com.example.app.repository.OtpRepository;
import com.example.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

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
    public boolean verifyOtpAndLogin(String email, String otpCode) {
        // 1. Find latest OTP for email
        Optional<Otp> otpOpt = otpRepository.findTopByEmailOrderByExpiryTimeDesc(email);

        if (otpOpt.isEmpty()) {
            return false; // No OTP found
        }

        Otp otp = otpOpt.get();

        // 2. Validate Code & Expiry
        if (!otp.verifyOtp(otpCode) || otp.isExpired()) {
            return false;
        }

        // 3. OTP Valid -> Create User if doesn't exist
        if (userRepository.findByEmail(email).isEmpty()) {
            User newUser = new User(email);
            userRepository.save(newUser);
        }

        // 4. Cleanup (In real app, maybe mark used instead of delete)
        otpRepository.deleteByEmail(email);

        return true;
    }
}
