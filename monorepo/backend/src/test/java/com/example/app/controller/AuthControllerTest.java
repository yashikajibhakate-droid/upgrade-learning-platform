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

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Test
    void testGenerateOtp_ReturnsOk() throws Exception {
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
