package com.example.app.config;

import com.example.app.model.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

  private final AntPathMatcher pathMatcher = new AntPathMatcher();
  private final List<String> publicPaths = Arrays.asList("/api/series/**", "/api/users/interests", "/api/health");

  @Autowired
  private com.example.app.service.AuthService authService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (request.getMethod().equals("OPTIONS"))
      return true;

    String path = request.getRequestURI();
    String method = request.getMethod();
    String emailParam = request.getParameter("email");

    boolean isPublic = false;
    // Endpoints with email query params are never public
    if (emailParam == null) {
      for (String pattern : publicPaths) {
        if (pathMatcher.match(pattern, path)) {
          // For series endpoints, only GET is public (to allow viewing
          // series/episodes/reviews)
          if (pattern.equals("/api/series/**")) {
            if (method.equals("GET")) {
              isPublic = true;
              break;
            }
          } else {
            isPublic = true;
            break;
          }
        }
      }
    }

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      Optional<Session> session = authService.getSession(token);
      if (session.isPresent()) {
        request.setAttribute("user", session.get().getUser());
        return true;
      }
    }

    if (isPublic) {
      return true;
    }

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return false;
  }
}
