package com.example.app.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
  void preHandle_PublicPathSeries_NoToken_ShouldAllow() throws Exception {
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/series/some-uuid");
    when(request.getParameter("email")).thenReturn(null);

    boolean result = authInterceptor.preHandle(request, response, new Object());

    assertTrue(result);
  }

  @Test
  void preHandle_PublicPathSeries_WithEmail_ShouldRequireAuth() throws Exception {
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/series/some-uuid");
    when(request.getParameter("email")).thenReturn("test@example.com");
    when(request.getHeader("Authorization")).thenReturn(null);

    boolean result = authInterceptor.preHandle(request, response, new Object());

    assertFalse(result);
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void preHandle_NonPublicPath_NoToken_ShouldRequireAuth() throws Exception {
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/watch-history");
    when(request.getParameter("email")).thenReturn(null);
    when(request.getHeader("Authorization")).thenReturn(null);

    boolean result = authInterceptor.preHandle(request, response, new Object());

    assertFalse(result);
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void preHandle_SeriesPost_NoToken_ShouldRequireAuth() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/api/series/some-uuid/reviews");
    when(request.getParameter("email")).thenReturn(null);
    when(request.getHeader("Authorization")).thenReturn(null);

    boolean result = authInterceptor.preHandle(request, response, new Object());

    assertFalse(result);
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  void preHandle_PublicPathUsersInterests_ShouldAllow() throws Exception {
    when(request.getMethod()).thenReturn("GET");
    when(request.getRequestURI()).thenReturn("/api/users/interests");
    when(request.getParameter("email")).thenReturn(null);

    boolean result = authInterceptor.preHandle(request, response, new Object());

    assertTrue(result);
  }

  @Test
  void preHandle_ProtectedPath_ValidToken_ShouldAllow() throws Exception {
    when(request.getMethod()).thenReturn("POST");
    when(request.getRequestURI()).thenReturn("/api/series/some-uuid/reviews");
    when(request.getParameter("email")).thenReturn(null);
    when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");

    User user = new User("test@example.com");
    Session session = new Session(user, "dummy-hash");

    when(authService.getSession("valid-token")).thenReturn(Optional.of(session));

    boolean result = authInterceptor.preHandle(request, response, new Object());

    assertTrue(result);
    verify(request).setAttribute("user", user);
  }
}
