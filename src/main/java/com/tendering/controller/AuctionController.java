package com.tendering.controller;

import com.tendering.dto.request.auction.AuctionCreateRequest;
import com.tendering.dto.request.auction.AuctionUpdateRequest;
import com.tendering.dto.response.auction.AuctionResponse;
import com.tendering.service.AuctionCrudService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionCrudService auctionCrudService;

    @PostMapping
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<AuctionResponse> createAuction(
            @Valid @RequestBody AuctionCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // In a real application, you would extract the user ID from the UserDetails
        UUID sellerPublicId = extractUserPublicId(userDetails);
        
        AuctionResponse response = auctionCrudService.createAuction(request, sellerPublicId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{publicId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<AuctionResponse> updateAuction(
            @PathVariable UUID publicId,
            @Valid @RequestBody AuctionUpdateRequest request) {
        AuctionResponse response = auctionCrudService.updateAuction(publicId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{publicId}")
    public ResponseEntity<AuctionResponse> getAuctionById(@PathVariable UUID publicId) {
        AuctionResponse response = auctionCrudService.getAuctionById(publicId);
        // Increment view count when viewing auction details
        auctionCrudService.incrementViewCount(publicId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<AuctionResponse>> getAllAuctions(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<AuctionResponse> response = auctionCrudService.getAllAuctions(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<AuctionResponse>> getAuctionsByStatus(
            @PathVariable String status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<AuctionResponse> response = auctionCrudService.getAuctionsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<AuctionResponse>> getAuctionsByType(
            @PathVariable String type,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<AuctionResponse> response = auctionCrudService.getAuctionsByType(type, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/seller/{sellerPublicId}")
    public ResponseEntity<Page<AuctionResponse>> getAuctionsBySeller(
            @PathVariable UUID sellerPublicId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<AuctionResponse> response = auctionCrudService.getAuctionsBySeller(sellerPublicId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-auctions")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Page<AuctionResponse>> getMyAuctions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        
        UUID sellerPublicId = extractUserPublicId(userDetails);
        Page<AuctionResponse> response = auctionCrudService.getAuctionsBySeller(sellerPublicId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<Page<AuctionResponse>> getActiveAuctions(
            @PageableDefault(size = 20, sort = "endTime") Pageable pageable) {
        Page<AuctionResponse> response = auctionCrudService.getActiveAuctions(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active/type/{type}")
    public ResponseEntity<Page<AuctionResponse>> getActiveAuctionsByType(
            @PathVariable String type,
            @PageableDefault(size = 20, sort = "endTime") Pageable pageable) {
        Page<AuctionResponse> response = auctionCrudService.getActiveAuctionsByType(type, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<AuctionResponse>> getUpcomingAuctions(
            @RequestParam(required = false) LocalDateTime start,
            @RequestParam(required = false) LocalDateTime end) {
        
        if (start == null) start = LocalDateTime.now();
        if (end == null) end = start.plusDays(7); // Next 7 days by default
        
        List<AuctionResponse> response = auctionCrudService.getUpcomingAuctions(start, end);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ending-soon")
    public ResponseEntity<List<AuctionResponse>> getEndingSoonAuctions(
            @RequestParam(defaultValue = "24") int hoursAhead) {
        List<AuctionResponse> response = auctionCrudService.getEndingSoonAuctions(hoursAhead);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<AuctionResponse>> getFeaturedAuctions() {
        List<AuctionResponse> response = auctionCrudService.getFeaturedAuctions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/live")
    public ResponseEntity<List<AuctionResponse>> getLiveAuctions() {
        List<AuctionResponse> response = auctionCrudService.getLiveAuctions();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/status")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<AuctionResponse> updateAuctionStatus(
            @PathVariable UUID publicId,
            @RequestParam String status) {
        AuctionResponse response = auctionCrudService.updateAuctionStatus(publicId, status);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{publicId}")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAuction(@PathVariable UUID publicId) {
        auctionCrudService.deleteAuction(publicId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{publicId}/toggle-featured")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuctionResponse> toggleFeatured(@PathVariable UUID publicId) {
        AuctionResponse response = auctionCrudService.toggleFeatured(publicId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{publicId}/toggle-live")
    @PreAuthorize("hasRole('SELLER') or hasRole('ADMIN')")
    public ResponseEntity<AuctionResponse> toggleLive(@PathVariable UUID publicId) {
        AuctionResponse response = auctionCrudService.toggleLive(publicId);
        return ResponseEntity.ok(response);
    }

    // Helper method to extract user public ID from UserDetails
    private UUID extractUserPublicId(UserDetails userDetails) {
        if (userDetails == null) {
            throw new RuntimeException("User is not authenticated!");
        }
        return UUID.fromString(userDetails.getUsername());
    }
}