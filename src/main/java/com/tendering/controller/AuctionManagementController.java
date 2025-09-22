package com.tendering.controller;

import com.tendering.dto.common.ApiResponse;
import com.tendering.dto.response.escrow.EscrowResponse;
import com.tendering.model.Bid;
import com.tendering.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/auction-management")
@RequiredArgsConstructor
@Slf4j
public class AuctionManagementController {

    private final AuctionService auctionService;

    @PostMapping("/{auctionId}/complete-with-escrow")
    public ResponseEntity<ApiResponse<EscrowResponse>> completeAuctionWithEscrow(
            @PathVariable String auctionId,
            @RequestParam(defaultValue = "0.05") BigDecimal commissionRate) {
        try {
            UUID auctionPublicId = UUID.fromString(auctionId);
            EscrowResponse escrow = auctionService.completeAuctionWithEscrow(
                    auctionPublicId, commissionRate);

            return ResponseEntity.ok(ApiResponse.success("İhale başarıyla tamamlandı ve ödeme escrow'a alındı", escrow));
        } catch (Exception e) {
            log.error("İhale escrow ile tamamlanırken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("İhale tamamlanamadı: " + e.getMessage()));
        }
    }

    @PostMapping("/{auctionId}/complete")
    public ResponseEntity<ApiResponse<Void>> completeAuctionWithoutEscrow(
            @PathVariable String auctionId) {
        try {
            UUID auctionPublicId = UUID.fromString(auctionId);
            auctionService.completeAuctionWithoutEscrow(auctionPublicId);

            return ResponseEntity.ok(ApiResponse.success("İhale başarıyla tamamlandı", null));
        } catch (Exception e) {
            log.error("İhale tamamlanırken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("İhale tamamlanamadı: " + e.getMessage()));
        }
    }

    @PostMapping("/{auctionId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelAuction(
            @PathVariable String auctionId,
            @RequestParam String reason) {
        try {
            UUID auctionPublicId = UUID.fromString(auctionId);
            auctionService.cancelAuction(auctionPublicId, reason);

            return ResponseEntity.ok(ApiResponse.success("İhale başarıyla iptal edildi", null));
        } catch (Exception e) {
            log.error("İhale iptal edilirken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("İhale iptal edilemedi: " + e.getMessage()));
        }
    }

    @GetMapping("/{auctionId}/winning-bid")
    public ResponseEntity<ApiResponse<BidInfo>> getWinningBid(
            @PathVariable String auctionId) {
        try {
            UUID auctionPublicId = UUID.fromString(auctionId);
            Bid winningBid = auctionService.getWinningBid(auctionPublicId);

            if (winningBid == null) {
                return ResponseEntity.ok(ApiResponse.success("İhale için kazanan teklif bulunamadı", null));
            }

            BidInfo bidInfo = BidInfo.builder()
                    .bidId(winningBid.getPublicId().toString())
                    .amount(winningBid.getAmount())
                    .bidderId(winningBid.getBidder().getPublicId().toString())
                    .bidderName(winningBid.getBidder().getName() + " " + winningBid.getBidder().getSurname())
                    .status(winningBid.getStatus())
                    .createdAt(winningBid.getCreatedAt())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Kazanan teklif başarıyla getirildi", bidInfo));
        } catch (Exception e) {
            log.error("Kazanan teklif getirilirken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Kazanan teklif getirilemedi: " + e.getMessage()));
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BidInfo {
        private String bidId;
        private BigDecimal amount;
        private String bidderId;
        private String bidderName;
        private String status;
        private LocalDateTime createdAt;
    }
}