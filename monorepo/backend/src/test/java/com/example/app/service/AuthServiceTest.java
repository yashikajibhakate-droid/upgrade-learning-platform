package com.example.app.service;

import com.example.app.model.Otp;
import com.example.app.repository.OtpRepository;
import com.example.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpRepository otpRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private String testEmail = "test@example.com";

    @Test
    void testGenerateAndSendOtp_Success() {
        // Arrange
        when(otpRepository.save(any(Otp.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        authService.generateAndSendOtp(testEmail);

        // Assert
        verify(emailService, times(1)).sendOtpEmail(eq(testEmail), anyString());
        verify(otpRepository, times(1)).save(any(Otp.class));
    }

    @Test
    void testVerifyOtpAndLogin_Success_NewUser() {
        // Arrange
        String validOtp = "123456";
        Otp otpEntity = new Otp(testEmail, validOtp, LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findTopByEmailOrderByExpiryTimeDesc(testEmail)).thenReturn(Optional.of(otpEntity));
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty()); // New user

        // Act
        boolean result = authService.verifyOtpAndLogin(testEmail, validOtp);

        // Assert
        assertTrue(result);
        verify(userRepository, times(1)).save(any());
        verify(otpRepository, times(1)).deleteByEmail(testEmail);
    }

    @Test
    void testVerifyOtpAndLogin_Failure_Expired() {
        // Arrange
        String expiredOtp = "123456";
        Otp otpEntity = new Otp(testEmail, expiredOtp, LocalDateTime.now().minusMinutes(1)); // Expired

        when(otpRepository.findTopByEmailOrderByExpiryTimeDesc(testEmail)).thenReturn(Optional.of(otpEntity));

        // Act
        boolean result = authService.verifyOtpAndLogin(testEmail, expiredOtp);

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testVerifyOtpAndLogin_Failure_InvalidCode() {
        // Arrange
        String savedOtp = "123456";
        String inputOtp = "654321";
        Otp otpEntity = new Otp(testEmail, savedOtp, LocalDateTime.now().plusMinutes(5));

        when(otpRepository.findTopByEmailOrderByExpiryTimeDesc(testEmail)).thenReturn(Optional.of(otpEntity));

        // Act
        boolean result = authService.verifyOtpAndLogin(testEmail, inputOtp);

        // Assert
        assertFalse(result);
    }
}
