package com.example.app.repository;

import com.example.app.model.Otp;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpRepository extends JpaRepository<Otp, UUID> {
  Optional<Otp> findTopByEmailOrderByExpiryTimeDesc(String email);

  void deleteByEmail(String email);
}
