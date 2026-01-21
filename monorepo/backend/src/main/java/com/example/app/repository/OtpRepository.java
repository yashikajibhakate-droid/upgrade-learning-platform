package com.example.app.repository;

import com.example.app.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, UUID> {
    Optional<Otp> findTopByEmailOrderByExpiryTimeDesc(String email);

    void deleteByEmail(String email);
}
