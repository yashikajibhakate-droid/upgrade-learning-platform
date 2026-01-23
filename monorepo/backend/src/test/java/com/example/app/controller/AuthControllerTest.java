package com.example.app.controller;

import com.example.app.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.bucket4j.ConsumptionProbe;
import static org.mockito.Mockito.mock;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.example.app.service.RateLimitingService rateLimitingService;

    @Test
    void testVerifyOtp_EmptyOtp_ReturnsBadRequest() throws Exception {
        String jsonBody = "{\"email\": \"test@example.com\", \"otp\": \"\"}";

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGenerateOtp_ReturnsOk() throws Exception {
        ConsumptionProbe probe = mock(ConsumptionProbe.class);
        when(probe.isConsumed()).thenReturn(true);
        when(rateLimitingService.resolveBucket(anyString())).thenReturn(probe);

        String jsonBody = "{\"email\": \"test@example.com\"}";

        mockMvc.perform(post("/api/auth/generate-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void testVerifyOtp_Success_ReturnsOk() throws Exception {
        when(authService.verifyOtpAndLogin(anyString(), anyString())).thenReturn(true);
        String jsonBody = "{\"email\": \"test@example.com\", \"otp\": \"123456\"}";

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void testVerifyOtp_Failure_ReturnsUnauthorized() throws Exception {
        when(authService.verifyOtpAndLogin(anyString(), anyString())).thenReturn(false);
        String jsonBody = "{\"email\": \"test@example.com\", \"otp\": \"000000\"}";

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody))
                .andExpect(status().isUnauthorized());
    }
}
