package com.tendering.repository;

import com.tendering.model.Auction;
import com.tendering.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    Optional<Auction> findByPublicId(UUID publicId);

    Page<Auction> findByStatus(String status, Pageable pageable);

    Page<Auction> findByType(Auction.AuctionType type, Pageable pageable);

    Page<Auction> findByTypeAndStatus(Auction.AuctionType type, String status, Pageable pageable);

    Page<Auction> findBySeller(User seller, Pageable pageable);

    @Query("SELECT a FROM Auction a WHERE a.startTime <= :now AND a.endTime >= :now AND a.status = 'ACTIVE'")
    Page<Auction> findActiveAuctions(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT a FROM Auction a WHERE a.type = :type AND a.startTime <= :now AND a.endTime >= :now AND a.status = 'ACTIVE'")
    Page<Auction> findActiveAuctionsByType(@Param("type") Auction.AuctionType type, @Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT a FROM Auction a WHERE a.startTime BETWEEN :start AND :end AND a.status = 'PENDING'")
    List<Auction> findUpcomingAuctions(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT a FROM Auction a WHERE a.endTime BETWEEN :now AND :endTime AND a.status = 'ACTIVE'")
    List<Auction> findEndingSoonAuctions(@Param("now") LocalDateTime now, @Param("endTime") LocalDateTime endTime);

    @Query("SELECT a FROM Auction a WHERE a.isFeatured = true AND a.status = 'ACTIVE'")
    List<Auction> findFeaturedAuctions();

    @Query("SELECT a FROM Auction a WHERE a.isLive = true AND a.status = 'ACTIVE'")
    List<Auction> findLiveAuctions();
}