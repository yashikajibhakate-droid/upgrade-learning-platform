package com.example.app.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // For development, we just log to console.
    public void sendOtpEmail(String toEmail, String otp) {
        System.out.println("================================");
        System.out.println("EMAILING OTP to: " + toEmail);
        System.out.println("OTP CODE: " + otp);
        System.out.println("================================");
    }
}
