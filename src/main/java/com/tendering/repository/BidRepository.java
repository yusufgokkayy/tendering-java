package com.tendering.repository;

import com.tendering.model.Auction;
import com.tendering.model.Bid;
import com.tendering.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    List<Bid> findByAuctionOrderByAmountDesc(Auction auction);

    Page<Bid> findByBidder(User bidder, Pageable pageable);

    Page<Bid> findByAuction(Auction auction, Pageable pageable);

    @Query("SELECT b FROM Bid b WHERE b.auction = :auction AND b.status = 'ACTIVE' ORDER BY b.amount DESC")
    List<Bid> findActiveHighestBidsByAuction(@Param("auction") Auction auction, Pageable pageable);

    @Query("SELECT MAX(b.amount) FROM Bid b WHERE b.auction = :auction AND b.status = 'ACTIVE'")
    Optional<BigDecimal> findHighestBidAmount(@Param("auction") Auction auction);

    @Query("SELECT COUNT(b) FROM Bid b WHERE b.auction = :auction")
    Long countBidsByAuction(@Param("auction") Auction auction);

    @Query("SELECT b FROM Bid b WHERE b.auction = :auction AND b.bidder = :bidder ORDER BY b.createdAt DESC")
    List<Bid> findByAuctionAndBidder(@Param("auction") Auction auction, @Param("bidder") User bidder);
}