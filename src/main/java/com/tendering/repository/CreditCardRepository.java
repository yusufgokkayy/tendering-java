package com.tendering.repository;

import com.tendering.model.CreditCard;
import com.tendering.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    List<CreditCard> findByUserAndIsActiveTrue(User user);

    Optional<CreditCard> findByUserAndIsDefaultTrueAndIsActiveTrue(User user);

    Optional<CreditCard> findByPublicId(UUID publicId);

    Optional<CreditCard> findByUserAndPublicId(User user, UUID publicId);

    boolean existsByUserAndMaskedCardNumber(User user, String maskedCardNumber);

    @Query("SELECT COUNT(cc) FROM CreditCard cc WHERE cc.user = :user AND cc.isActive = true")
    long countActiveByUser(@Param("user") User user);

    List<CreditCard> findByUserAndIsVerifiedTrueAndIsActiveTrue(User user);
}