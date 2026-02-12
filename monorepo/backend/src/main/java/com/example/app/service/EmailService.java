package com.example.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

  @Autowired private JavaMailSender mailSender;

  @org.springframework.beans.factory.annotation.Value("${app.mail.from}")
  private String fromEmail;

  private static final org.slf4j.Logger logger =
      org.slf4j.LoggerFactory.getLogger(EmailService.class);

  @org.springframework.scheduling.annotation.Async
  public void sendOtpEmail(String toEmail, String otp) {
    if (fromEmail == null || fromEmail.trim().isEmpty()) {
      logger.warn("Email sending skipped: 'app.mail.from' is not configured.");
      return;
    }
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(toEmail);
      message.setSubject("Your Login OTP");
      message.setText("Your OTP for login is: " + otp + "\n\nThis code expires in 5 minutes.");

      mailSender.send(message);
      logger.info("Email sent successfully to: {}", toEmail);
    } catch (Exception e) {
      logger.error("Failed to send email: {}", e.getMessage());
    }
  }

  @org.springframework.scheduling.annotation.Async
  public void sendDailyReminder(String toEmail, String subject, String content) {
    if (fromEmail == null || fromEmail.trim().isEmpty()) {
      logger.warn("Email sending skipped: 'app.mail.from' is not configured.");
      return;
    }
    try {
      SimpleMailMessage message = new SimpleMailMessage();
      message.setFrom(fromEmail);
      message.setTo(toEmail);
      message.setSubject(subject);
      message.setText(content);

      mailSender.send(message);
      logger.info("Daily reminder email sent successfully to: {}", toEmail);
    } catch (Exception e) {
      logger.error("Failed to send daily reminder email: {}", e.getMessage());
    }
  }
}
