package com.tendering.repository;

import com.tendering.model.PasswordResetToken; // entity değil model
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

    Optional<PasswordResetToken> findByEmailAndUsedFalse(String email);

    void deleteByExpiryDateBefore(LocalDateTime dateTime);

    void deleteByEmail(String email);
}