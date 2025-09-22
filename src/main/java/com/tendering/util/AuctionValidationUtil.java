package com.tendering.util;

import com.tendering.model.Auction;
import com.tendering.model.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AuctionValidationUtil {

    /**
     * Validates if auction timing is valid
     */
    public boolean isValidAuctionTiming(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        return startTime.isAfter(now) && endTime.isAfter(startTime);
    }

    /**
     * Checks if user can bid on auction
     */
    public boolean canUserBidOnAuction(Auction auction, User bidder) {
        // User cannot bid on their own auction
        if (auction.getSeller().equals(bidder)) {
            return false;
        }

        // Auction must be active
        if (!"ACTIVE".equals(auction.getStatus())) {
            return false;
        }

        // Auction must be in active time range
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(auction.getStartTime()) && !now.isAfter(auction.getEndTime());
    }

    /**
     * Checks if auction can be modified
     */
    public boolean canModifyAuction(Auction auction) {
        // Cannot modify auction that has started or has bids
        if ("ACTIVE".equals(auction.getStatus()) && auction.getStartTime().isBefore(LocalDateTime.now())) {
            return false;
        }
        
        return auction.getBids().isEmpty();
    }

    /**
     * Checks if auction can be deleted
     */
    public boolean canDeleteAuction(Auction auction) {
        // Cannot delete active auction with bids
        return !("ACTIVE".equals(auction.getStatus()) && !auction.getBids().isEmpty());
    }

    /**
     * Calculates time remaining for auction
     */
    public long getTimeRemainingInMinutes(Auction auction) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getEndTime())) {
            return 0;
        }
        return java.time.Duration.between(now, auction.getEndTime()).toMinutes();
    }
}