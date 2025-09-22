package com.tendering.service.impl;

import com.tendering.dto.request.bid.BidCreateRequest;
import com.tendering.dto.response.bid.BidResponse;
import com.tendering.exceptionHandlers.ResourceNotFoundException;
import com.tendering.model.Auction;
import com.tendering.model.Bid;
import com.tendering.model.User;
import com.tendering.repository.AuctionRepository;
import com.tendering.repository.BidRepository;
import com.tendering.repository.UserRepository;
import com.tendering.service.BidService;
import com.tendering.util.AuctionValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BidServiceImpl implements BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final AuctionValidationUtil auctionValidationUtil;

    @Override
    public BidResponse createBid(BidCreateRequest request, UUID bidderPublicId) {
        log.debug("Creating new bid for auction: {} by bidder: {}", request.getAuctionPublicId(), bidderPublicId);

        // Get auction
        Auction auction = auctionRepository.findByPublicId(request.getAuctionPublicId())
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + request.getAuctionPublicId()));

        // Get bidder
        User bidder = userRepository.findByPublicId(bidderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + bidderPublicId));

        // Validate bid using utility
        if (!auctionValidationUtil.canUserBidOnAuction(auction, bidder)) {
            throw new IllegalStateException("Bu ihalede teklif veremezsiniz");
        }

        // Validate bid amount
        if (!canBid(request.getAuctionPublicId(), bidderPublicId, request.getAmount())) {
            throw new IllegalStateException("Bu teklifi veremezsiniz");
        }

        // Create bid
        Bid bid = Bid.builder()
                .auction(auction)
                .bidder(bidder)
                .amount(request.getAmount())
                .build();

        Bid savedBid = bidRepository.save(bid);

        // Update auction current price
        auction.setCurrentPrice(request.getAmount());
        auctionRepository.save(auction);

        log.debug("Bid created successfully with ID: {}", savedBid.getPublicId());
        return BidResponse.fromEntity(savedBid);
    }

    @Override
    @Transactional(readOnly = true)
    public BidResponse getBidById(UUID publicId) {
        log.debug("Getting bid by ID: {}", publicId);

        Bid bid = bidRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Teklif bulunamadı: " + publicId));

        return BidResponse.fromEntity(bid);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BidResponse> getBidsByAuction(UUID auctionPublicId, Pageable pageable) {
        log.debug("Getting bids for auction: {}", auctionPublicId);

        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + auctionPublicId));

        Page<Bid> bidPage = bidRepository.findByAuction(auction, pageable);
        return bidPage.map(BidResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BidResponse> getBidsByBidder(UUID bidderPublicId, Pageable pageable) {
        log.debug("Getting bids for bidder: {}", bidderPublicId);

        User bidder = userRepository.findByPublicId(bidderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + bidderPublicId));

        Page<Bid> bidPage = bidRepository.findByBidder(bidder, pageable);
        return bidPage.map(BidResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidResponse> getHighestBidsByAuction(UUID auctionPublicId, int limit) {
        log.debug("Getting highest {} bids for auction: {}", limit, auctionPublicId);

        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + auctionPublicId));

        Pageable pageable = PageRequest.of(0, limit);
        List<Bid> highestBids = bidRepository.findActiveHighestBidsByAuction(auction, pageable);

        return highestBids.stream()
                .map(BidResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getHighestBidAmount(UUID auctionPublicId) {
        log.debug("Getting highest bid amount for auction: {}", auctionPublicId);

        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + auctionPublicId));

        Optional<BigDecimal> highestAmount = bidRepository.findHighestBidAmount(auction);
        return highestAmount.orElse(auction.getStartPrice());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getBidCount(UUID auctionPublicId) {
        log.debug("Getting bid count for auction: {}", auctionPublicId);

        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + auctionPublicId));

        return bidRepository.countBidsByAuction(auction);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidResponse> getBidsByAuctionAndBidder(UUID auctionPublicId, UUID bidderPublicId) {
        log.debug("Getting bids for auction: {} and bidder: {}", auctionPublicId, bidderPublicId);

        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + auctionPublicId));

        User bidder = userRepository.findByPublicId(bidderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + bidderPublicId));

        List<Bid> bids = bidRepository.findByAuctionAndBidder(auction, bidder);
        return bids.stream()
                .map(BidResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public BidResponse cancelBid(UUID bidPublicId) {
        log.debug("Canceling bid: {}", bidPublicId);

        Bid bid = bidRepository.findByPublicId(bidPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Teklif bulunamadı: " + bidPublicId));

        // Check if bid can be canceled
        if (!"ACTIVE".equals(bid.getStatus())) {
            throw new IllegalStateException("Sadece aktif teklifler iptal edilebilir");
        }

        // Check if auction is still active and hasn't ended
        Auction auction = bid.getAuction();
        if (!"ACTIVE".equals(auction.getStatus()) || auction.getEndTime().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("İhale sonlandığı için teklif iptal edilemez");
        }

        bid.setStatus("CANCELED");
        Bid updatedBid = bidRepository.save(bid);

        log.debug("Bid canceled successfully: {}", bidPublicId);
        return BidResponse.fromEntity(updatedBid);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canBid(UUID auctionPublicId, UUID bidderPublicId, BigDecimal amount) {
        log.debug("Checking if bidder {} can bid {} on auction {}", bidderPublicId, amount, auctionPublicId);

        // Get auction
        Auction auction = auctionRepository.findByPublicId(auctionPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("İhale bulunamadı: " + auctionPublicId));

        // Check auction status
        if (!"ACTIVE".equals(auction.getStatus())) {
            log.debug("Auction is not active: {}", auction.getStatus());
            return false;
        }

        // Check auction timing
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(auction.getStartTime()) || now.isAfter(auction.getEndTime())) {
            log.debug("Auction is not in active time range");
            return false;
        }

        // Check if bidder is not the seller
        User bidder = userRepository.findByPublicId(bidderPublicId)
                .orElseThrow(() -> new ResourceNotFoundException("Kullanıcı bulunamadı: " + bidderPublicId));
        
        if (bidder.equals(auction.getSeller())) {
            log.debug("Bidder cannot bid on their own auction");
            return false;
        }

        // Check minimum bid amount
        BigDecimal currentPrice = auction.getCurrentPrice();
        BigDecimal minimumBid = currentPrice.add(auction.getMinBidIncrement());
        
        if (amount.compareTo(minimumBid) < 0) {
            log.debug("Bid amount {} is less than minimum required: {}", amount, minimumBid);
            return false;
        }

        return true;
    }
}