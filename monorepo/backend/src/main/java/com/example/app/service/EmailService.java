package com.example.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Autowired
  private JavaMailSender mailSender;

  @org.springframework.beans.factory.annotation.Value("${app.mail.from}")
  private String fromEmail;

  @org.springframework.scheduling.annotation.Async
  public void sendOtpEmail(String toEmail, String otp) {
    if (fromEmail == null || fromEmail.trim().isEmpty()) {
      System.err.println("Email sending skipped: 'app.mail.from' is not configured.");
      System.out.println("fallback OTP: " + otp);
      return;
    }
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(toEmail);
      message.setSubject("Your Login OTP");
      message.setText("Your OTP for login is: " + otp + "\n\nThis code expires in 5 minutes.");

      mailSender.send(message);
      System.out.println("Email sent successfully to: " + toEmail);
    } catch (Exception e) {
      System.err.println("Failed to send email: " + e.getMessage());
      // Fallback to console for dev if email fails
      System.out.println("fallback OTP: " + otp);
    }
  }
}
