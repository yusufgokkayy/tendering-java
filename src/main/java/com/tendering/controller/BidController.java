package com.tendering.controller;

import com.tendering.dto.request.bid.BidCreateRequest;
import com.tendering.dto.response.bid.BidResponse;
import com.tendering.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER')")
    public ResponseEntity<BidResponse> createBid(
            @Valid @RequestBody BidCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real application, you would extract the user ID from the UserDetails
        // For now, we'll need to get it from the request or session
        // This is a simplified approach - you'll need to implement proper user extraction
        UUID bidderPublicId = extractUserPublicId(userDetails);
        
        BidResponse response = bidService.createBid(request, bidderPublicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<BidResponse> getBidById(@PathVariable UUID publicId) {
        BidResponse response = bidService.getBidById(publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auction/{auctionPublicId}")
    public ResponseEntity<Page<BidResponse>> getBidsByAuction(
            @PathVariable UUID auctionPublicId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<BidResponse> response = bidService.getBidsByAuction(auctionPublicId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-bids")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER')")
    public ResponseEntity<Page<BidResponse>> getMyBids(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        UUID bidderPublicId = extractUserPublicId(userDetails);
        Page<BidResponse> response = bidService.getBidsByBidder(bidderPublicId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auction/{auctionPublicId}/highest")
    public ResponseEntity<List<BidResponse>> getHighestBids(
            @PathVariable UUID auctionPublicId,
            @RequestParam(defaultValue = "10") int limit) {
        List<BidResponse> response = bidService.getHighestBidsByAuction(auctionPublicId, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auction/{auctionPublicId}/highest-amount")
    public ResponseEntity<BigDecimal> getHighestBidAmount(@PathVariable UUID auctionPublicId) {
        BigDecimal amount = bidService.getHighestBidAmount(auctionPublicId);
        return ResponseEntity.ok(amount);
    }

    @GetMapping("/auction/{auctionPublicId}/count")
    public ResponseEntity<Long> getBidCount(@PathVariable UUID auctionPublicId) {
        Long count = bidService.getBidCount(auctionPublicId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/auction/{auctionPublicId}/my")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER')")
    public ResponseEntity<List<BidResponse>> getMyBidsForAuction(
            @PathVariable UUID auctionPublicId,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID bidderPublicId = extractUserPublicId(userDetails);
        List<BidResponse> response = bidService.getBidsByAuctionAndBidder(auctionPublicId, bidderPublicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/cancel")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER')")
    public ResponseEntity<BidResponse> cancelBid(@PathVariable UUID publicId) {
        BidResponse response = bidService.cancelBid(publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auction/{auctionPublicId}/can-bid")
    @PreAuthorize("hasRole('BUYER') or hasRole('SELLER')")
    public ResponseEntity<Boolean> canBid(
            @PathVariable UUID auctionPublicId,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        UUID bidderPublicId = extractUserPublicId(userDetails);
        boolean canBid = bidService.canBid(auctionPublicId, bidderPublicId, amount);
        return ResponseEntity.ok(canBid);
    }

    // Helper method to extract user public ID from UserDetails
    private UUID extractUserPublicId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User is not authenticated!");
        }
        return UUID.fromString(userDetails.getUsername());
    }
}