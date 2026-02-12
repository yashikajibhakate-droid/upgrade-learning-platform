package com.example.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

  @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
  private String allowedOrigins;

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        java.util.List<String> originList = new java.util.ArrayList<>();
        if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
          java.util.Arrays.stream(allowedOrigins.split(","))
              .map(String::trim)
              .filter(s -> !s.isEmpty())
              .map(s -> s.endsWith("/") ? s.substring(0, s.length() - 1) : s)
              .forEach(originList::add);
        }

        String[] origins = originList.toArray(new String[0]);

        registry
            .addMapping("/**")
            .allowedOrigins(origins)
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
      }
    };
  }
}
