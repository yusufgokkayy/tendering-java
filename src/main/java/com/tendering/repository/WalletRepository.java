package com.tendering.repository;

import com.tendering.model.User;
import com.tendering.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByUser(User user);

    Optional<Wallet> findByUserPublicId(UUID userPublicId);

    Optional<Wallet> findByPublicId(UUID publicId);

    boolean existsByUser(User user);
}