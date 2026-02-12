package com.example.app.config;

import com.example.app.model.Session;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

  private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

  @Autowired
  private com.example.app.service.AuthService authService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    log.debug("Processing request: {} Method: {}", request.getRequestURI(), request.getMethod());
    if (request.getMethod().equals("OPTIONS"))
      return true;

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      Optional<Session> session = authService.getSession(token);
      if (session.isPresent()) {
        request.setAttribute("user", session.get().getUser());
        return true;
      }
    }

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    return false;
  }
}
