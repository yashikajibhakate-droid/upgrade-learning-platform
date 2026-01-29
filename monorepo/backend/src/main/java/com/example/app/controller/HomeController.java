package com.example.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

  public HomeController() {
    System.out.println("HomeController Initialized!");
  }

  @GetMapping("/")
  public String home() {
    return "Welcome to the Monorepo Backend API! Check /api/health for status.";
  }

  @GetMapping("/home")
  public String homeAlt() {
    return "Home Alternative";
  }
}
