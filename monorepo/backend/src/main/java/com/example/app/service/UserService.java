package com.example.app.service;

import com.example.app.model.User;
import com.example.app.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

  @Autowired private UserRepository userRepository;

  // Hardcoded list of interests for now. In a real app, this might come from a DB
  // table.
  private static final List<String> AVAILABLE_INTERESTS =
      List.of(
          "Python Programming",
          "Data Science",
          "UI/UX Design",
          "Digital Marketing",
          "Cloud Computing",
          "Cybersecurity",
          "React Framework",
          "Personal Finance");

  public List<String> getAvailableInterests() {
    return AVAILABLE_INTERESTS;
  }

  @Transactional
  public void updateInterests(String email, Set<String> interests) {
    Optional<User> userOpt = userRepository.findByEmail(email);
    if (userOpt.isPresent()) {
      User user = userOpt.get();
      // Validate interests against available list if needed, or just save
      user.setInterests(interests);
      userRepository.save(user); // JPA ElementCollection will update automatically
    } else {
      throw new IllegalArgumentException("User not found with email: " + email);
    }
  }
}
