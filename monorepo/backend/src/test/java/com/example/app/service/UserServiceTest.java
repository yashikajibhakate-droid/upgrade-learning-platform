package com.example.app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.app.model.User;
import com.example.app.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @InjectMocks private UserService userService;

  @Test
  void testGetAvailableInterests_ReturnsList() {
    assertFalse(userService.getAvailableInterests().isEmpty());
  }

  @Test
  void testUpdateInterests_Success() {
    String email = "test@example.com";
    User user = new User(email);
    Set<String> interests = Set.of("Tech", "Design");

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    userService.updateInterests(email, interests);

    verify(userRepository, times(1)).save(user);
    assertEquals(interests, user.getInterests());
  }

  @Test
  void testUpdateInterests_UserNotFound_ThrowsException() {
    String email = "unknown@example.com";
    Set<String> interests = Set.of("Tech");

    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class, () -> userService.updateInterests(email, interests));
    verify(userRepository, never()).save(any());
  }
}
