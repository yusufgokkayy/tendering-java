package com.tendering.repository;

import com.tendering.model.Auction;
import com.tendering.model.Escrow;
import com.tendering.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EscrowRepository extends JpaRepository<Escrow, Long> {

    Optional<Escrow> findByPublicId(UUID publicId);

    Optional<Escrow> findByAuction(Auction auction);

    List<Escrow> findByBuyer(User buyer);

    List<Escrow> findBySeller(User seller);

    List<Escrow> findByStatus(Escrow.EscrowStatus status);

    @Query("SELECT e FROM Escrow e WHERE e.status = :status AND e.autoReleaseDate <= :currentTime")
    List<Escrow> findByStatusAndAutoReleaseDateBefore(
            @Param("status") Escrow.EscrowStatus status, 
            @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT e FROM Escrow e WHERE e.buyer = :user OR e.seller = :user")
    List<Escrow> findByBuyerOrSeller(@Param("user") User user);

    boolean existsByAuction(Auction auction);
}