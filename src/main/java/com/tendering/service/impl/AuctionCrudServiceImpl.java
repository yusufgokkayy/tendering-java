package com.tendering.service.impl;

import com.tendering.dto.request.auction.AuctionCreateRequest;
import com.tendering.dto.request.auction.AuctionUpdateRequest;
import com.tendering.dto.response.auction.AuctionResponse;
import com.tendering.exceptionHandlers.ResourceNotFoundException;
import com.tendering.model.Auction;
import com.tendering.model.User;
import com.tendering.repository.AuctionRepository;
import com.tendering.repository.UserRepository;
import com.tendering.service.AuctionCrudService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuctionCrudServiceImpl implements AuctionCrudService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;

    @Override
    public AuctionResponse createAuction(AuctionCreateRequest request, UUID sellerPublicId) {
        log.debug("Creating new auction: {} by seller: {}", request.getTitle(), sellerPublicId);

        // Get seller
        User seller = userRepository.findByPublicId(sellerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Satıcı bulunamadı: " + sellerPublicId));

        // Validate end time is after start time
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("Bitiş zamanı başlangıç zamanından sonra olmalıdır");
        }

        // Create auction
        Auction auction = Auction.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .seller(seller)
                .type(Auction.AuctionType.valueOf(request.getType().toUpperCase()))
                .startPrice(request.getStartPrice())
                .reservePrice(request.getReservePrice())
                .minBidIncrement(request.getMinBidIncrement())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .thumbnailUrl(request.getThumbnailUrl())
                .isFeatured(request.getIsFeatured())
                .isLive(request.getIsLive())
                .streamUrl(request.getStreamUrl())
                .chatEnabled(request.getChatEnabled())
                .build();

        Auction savedAuction = auctionRepository.save(auction);
        log.debug("Auction created successfully with ID: {}", savedAuction.getPublicId());

        return AuctionResponse.fromEntity(savedAuction);
    }

    @Override
    public AuctionResponse updateAuction(UUID publicId, AuctionUpdateRequest request) {
        log.debug("Updating auction: {}", publicId);

        Auction auction = auctionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + publicId));

        // Check if auction can be updated (not started yet or in PENDING status)
        if ("ACTIVE".equals(auction.getStatus()) && auction.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Başlamış ihale güncellenemez");
        }

        // Update fields if provided
        if (request.getTitle() != null) {
            auction.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            auction.setDescription(request.getDescription());
        }
        if (request.getStartPrice() != null) {
            auction.setStartPrice(request.getStartPrice());
            // Update current price if no bids yet
            if (auction.getBids().isEmpty()) {
                auction.setCurrentPrice(request.getStartPrice());
            }
        }
        if (request.getReservePrice() != null) {
            auction.setReservePrice(request.getReservePrice());
        }
        if (request.getMinBidIncrement() != null) {
            auction.setMinBidIncrement(request.getMinBidIncrement());
        }
        if (request.getStartTime() != null) {
            auction.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            auction.setEndTime(request.getEndTime());
        }
        if (request.getThumbnailUrl() != null) {
            auction.setThumbnailUrl(request.getThumbnailUrl());
        }
        if (request.getIsFeatured() != null) {
            auction.setIsFeatured(request.getIsFeatured());
        }
        if (request.getIsLive() != null) {
            auction.setIsLive(request.getIsLive());
        }
        if (request.getStreamUrl() != null) {
            auction.setStreamUrl(request.getStreamUrl());
        }
        if (request.getChatEnabled() != null) {
            auction.setChatEnabled(request.getChatEnabled());
        }
        if (request.getStatus() != null) {
            auction.setStatus(request.getStatus());
        }

        // Validate end time is after start time
        if (auction.getEndTime().isBefore(auction.getStartTime())) {
            throw new IllegalArgumentException("Bitiş zamanı başlangıç zamanından sonra olmalıdır");
        }

        Auction updatedAuction = auctionRepository.save(auction);
        log.debug("Auction updated successfully: {}", publicId);

        return AuctionResponse.fromEntity(updatedAuction);
    }

    @Override
    @Transactional(readOnly = true)
    public AuctionResponse getAuctionById(UUID publicId) {
        log.debug("Getting auction by ID: {}", publicId);

        Auction auction = auctionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + publicId));

        return AuctionResponse.fromEntity(auction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponse> getAllAuctions(Pageable pageable) {
        log.debug("Getting all auctions with pagination: {}", pageable);

        Page<Auction> auctionPage = auctionRepository.findAll(pageable);
        return auctionPage.map(AuctionResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponse> getAuctionsByStatus(String status, Pageable pageable) {
        log.debug("Getting auctions by status: {}", status);

        Page<Auction> auctionPage = auctionRepository.findByStatus(status, pageable);
        return auctionPage.map(AuctionResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponse> getAuctionsByType(String type, Pageable pageable) {
        log.debug("Getting auctions by type: {}", type);

        Auction.AuctionType auctionType = Auction.AuctionType.valueOf(type.toUpperCase());
        Page<Auction> auctionPage = auctionRepository.findByType(auctionType, pageable);
        return auctionPage.map(AuctionResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponse> getAuctionsBySeller(UUID sellerPublicId, Pageable pageable) {
        log.debug("Getting auctions by seller: {}", sellerPublicId);

        User seller = userRepository.findByPublicId(sellerPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Satıcı bulunamadı: " + sellerPublicId));

        Page<Auction> auctionPage = auctionRepository.findBySeller(seller, pageable);
        return auctionPage.map(AuctionResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponse> getActiveAuctions(Pageable pageable) {
        log.debug("Getting active auctions");

        LocalDateTime now = LocalDateTime.now();
        Page<Auction> auctionPage = auctionRepository.findActiveAuctions(now, pageable);
        return auctionPage.map(AuctionResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionResponse> getActiveAuctionsByType(String type, Pageable pageable) {
        log.debug("Getting active auctions by type: {}", type);

        Auction.AuctionType auctionType = Auction.AuctionType.valueOf(type.toUpperCase());
        LocalDateTime now = LocalDateTime.now();
        Page<Auction> auctionPage = auctionRepository.findActiveAuctionsByType(auctionType, now, pageable);
        return auctionPage.map(AuctionResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuctionResponse> getUpcomingAuctions(LocalDateTime start, LocalDateTime end) {
        log.debug("Getting upcoming auctions between {} and {}", start, end);

        List<Auction> auctions = auctionRepository.findUpcomingAuctions(start, end);
        return auctions.stream()
                .map(AuctionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuctionResponse> getEndingSoonAuctions(int hoursAhead) {
        log.debug("Getting auctions ending in the next {} hours", hoursAhead);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = now.plusHours(hoursAhead);
        List<Auction> auctions = auctionRepository.findEndingSoonAuctions(now, endTime);
        return auctions.stream()
                .map(AuctionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuctionResponse> getFeaturedAuctions() {
        log.debug("Getting featured auctions");

        List<Auction> auctions = auctionRepository.findFeaturedAuctions();
        return auctions.stream()
                .map(AuctionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuctionResponse> getLiveAuctions() {
        log.debug("Getting live auctions");

        List<Auction> auctions = auctionRepository.findLiveAuctions();
        return auctions.stream()
                .map(AuctionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public AuctionResponse updateAuctionStatus(UUID publicId, String status) {
        log.debug("Updating auction status: {} to {}", publicId, status);

        Auction auction = auctionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + publicId));

        auction.setStatus(status);
        Auction updatedAuction = auctionRepository.save(auction);

        log.debug("Auction status updated successfully: {}", publicId);
        return AuctionResponse.fromEntity(updatedAuction);
    }

    @Override
    public void deleteAuction(UUID publicId) {
        log.debug("Deleting auction: {}", publicId);

        Auction auction = auctionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + publicId));

        // Check if auction can be deleted (not started or has no bids)
        if ("ACTIVE".equals(auction.getStatus()) && !auction.getBids().isEmpty()) {
            throw new IllegalStateException("Aktif ve teklifli ihale silinemez");
        }

        auctionRepository.delete(auction);
        log.debug("Auction deleted successfully: {}", publicId);
    }

    @Override
    public AuctionResponse incrementViewCount(UUID publicId) {
        log.debug("Incrementing view count for auction: {}", publicId);

        Auction auction = auctionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + publicId));

        auction.setViewCount(auction.getViewCount() + 1);
        Auction updatedAuction = auctionRepository.save(auction);

        return AuctionResponse.fromEntity(updatedAuction);
    }

    @Override
    public AuctionResponse toggleFeatured(UUID publicId) {
        log.debug("Toggling featured status for auction: {}", publicId);

        Auction auction = auctionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + publicId));

        auction.setIsFeatured(!auction.getIsFeatured());
        Auction updatedAuction = auctionRepository.save(auction);

        log.debug("Featured status toggled for auction: {} to {}", publicId, auction.getIsFeatured());
        return AuctionResponse.fromEntity(updatedAuction);
    }

    @Override
    public AuctionResponse toggleLive(UUID publicId) {
        log.debug("Toggling live status for auction: {}", publicId);

        Auction auction = auctionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + publicId));

        auction.setIsLive(!auction.getIsLive());
        Auction updatedAuction = auctionRepository.save(auction);

        log.debug("Live status toggled for auction: {} to {}", publicId, auction.getIsLive());
        return AuctionResponse.fromEntity(updatedAuction);
    }
}