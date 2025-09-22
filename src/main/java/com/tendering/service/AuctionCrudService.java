package com.tendering.service;

import com.tendering.dto.request.auction.AuctionCreateRequest;
import com.tendering.dto.request.auction.AuctionUpdateRequest;
import com.tendering.dto.response.auction.AuctionResponse;
import com.tendering.model.Auction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AuctionCrudService {

    AuctionResponse createAuction(AuctionCreateRequest request, UUID sellerPublicId);

    AuctionResponse updateAuction(UUID publicId, AuctionUpdateRequest request);

    AuctionResponse getAuctionById(UUID publicId);

    Page<AuctionResponse> getAllAuctions(Pageable pageable);

    Page<AuctionResponse> getAuctionsByStatus(String status, Pageable pageable);

    Page<AuctionResponse> getAuctionsByType(String type, Pageable pageable);

    Page<AuctionResponse> getAuctionsBySeller(UUID sellerPublicId, Pageable pageable);

    Page<AuctionResponse> getActiveAuctions(Pageable pageable);

    Page<AuctionResponse> getActiveAuctionsByType(String type, Pageable pageable);

    List<AuctionResponse> getUpcomingAuctions(LocalDateTime start, LocalDateTime end);

    List<AuctionResponse> getEndingSoonAuctions(int hoursAhead);

    List<AuctionResponse> getFeaturedAuctions();

    List<AuctionResponse> getLiveAuctions();

    AuctionResponse updateAuctionStatus(UUID publicId, String status);

    void deleteAuction(UUID publicId);

    AuctionResponse incrementViewCount(UUID publicId);

    AuctionResponse toggleFeatured(UUID publicId);

    AuctionResponse toggleLive(UUID publicId);
}