package com.example.app.config;

import com.example.app.model.Session;
import com.example.app.model.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthInterceptorTest {

    @Mock
    private com.example.app.service.AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthInterceptor authInterceptor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void preHandle_OptionsRequest_ShouldAllow() throws Exception {
        when(request.getMethod()).thenReturn("OPTIONS");

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
    }

    @Test
    void preHandle_ProtectedPath_NoToken_ShouldFail() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/series");
        when(request.getHeader("Authorization")).thenReturn(null);

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void preHandle_ProtectedPath_InvalidToken_ShouldFail() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/series");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(authService.getSession(anyString())).thenReturn(Optional.empty());

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertFalse(result);
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void preHandle_ProtectedPath_ValidToken_ShouldAllow() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/series");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");

        User user = new User("test@example.com");
        // We use a dummy hash since we are mocking authService anyway
        Session session = new Session(user, "dummy-hash");

        when(authService.getSession("valid-token")).thenReturn(Optional.of(session));

        boolean result = authInterceptor.preHandle(request, response, new Object());

        assertTrue(result);
        verify(request).setAttribute("user", user);
    }
}
