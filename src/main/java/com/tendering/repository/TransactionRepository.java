package com.tendering.repository;

import com.tendering.model.Transaction;
import com.tendering.model.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Page<Transaction> findByWalletOrderByCreatedAtDesc(Wallet wallet, Pageable pageable);

    Page<Transaction> findByWalletPublicIdOrderByCreatedAtDesc(UUID walletPublicId, Pageable pageable);

    List<Transaction> findByWalletAndCreatedAtBetweenOrderByCreatedAtDesc(
            Wallet wallet, LocalDateTime start, LocalDateTime end);

    Optional<Transaction> findByPublicId(UUID publicId);

    Optional<Transaction> findByReferenceId(String referenceId);
}