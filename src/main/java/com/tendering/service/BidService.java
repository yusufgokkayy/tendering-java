package com.tendering.service;

import com.tendering.dto.request.bid.BidCreateRequest;
import com.tendering.dto.response.bid.BidResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BidService {

    BidResponse createBid(BidCreateRequest request, UUID bidderPublicId);

    BidResponse getBidById(UUID publicId);

    Page<BidResponse> getBidsByAuction(UUID auctionPublicId, Pageable pageable);

    Page<BidResponse> getBidsByBidder(UUID bidderPublicId, Pageable pageable);

    List<BidResponse> getHighestBidsByAuction(UUID auctionPublicId, int limit);

    BigDecimal getHighestBidAmount(UUID auctionPublicId);

    Long getBidCount(UUID auctionPublicId);

    List<BidResponse> getBidsByAuctionAndBidder(UUID auctionPublicId, UUID bidderPublicId);

    BidResponse cancelBid(UUID bidPublicId);

    boolean canBid(UUID auctionPublicId, UUID bidderPublicId, BigDecimal amount);
}