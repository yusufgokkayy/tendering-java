package com.tendering.repository;

import com.tendering.model.BankAccount;
import com.tendering.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    List<BankAccount> findByUserAndIsActiveTrue(User user);

    Optional<BankAccount> findByUserAndIsDefaultTrueAndIsActiveTrue(User user);

    Optional<BankAccount> findByPublicId(UUID publicId);

    Optional<BankAccount> findByUserAndPublicId(User user, UUID publicId);

    boolean existsByUserAndIban(User user, String iban);

    @Query("SELECT COUNT(ba) FROM BankAccount ba WHERE ba.user = :user AND ba.isActive = true")
    long countActiveByUser(@Param("user") User user);

    List<BankAccount> findByUserAndIsVerifiedTrueAndIsActiveTrue(User user);
}