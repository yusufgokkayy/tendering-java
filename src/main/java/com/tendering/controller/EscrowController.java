package com.tendering.controller;

import com.tendering.dto.common.ApiResponse;
import com.tendering.dto.response.escrow.EscrowResponse;
import com.tendering.service.EscrowService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/escrow")
@RequiredArgsConstructor
@Slf4j
public class EscrowController {

    private final EscrowService escrowService;

    @GetMapping("/my-escrows")
    public ResponseEntity<ApiResponse<List<EscrowResponse>>> getUserEscrows(
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            List<EscrowResponse> escrows = escrowService.getUserEscrows(userPublicId);

            return ResponseEntity.ok(ApiResponse.success("Escrow'lar başarıyla getirildi", escrows));
        } catch (Exception e) {
            log.error("Escrow'lar getirilirken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Escrow'lar getirilemedi: " + e.getMessage()));
        }
    }

    @GetMapping("/{escrowId}")
    public ResponseEntity<ApiResponse<EscrowResponse>> getEscrowDetails(
            @PathVariable String escrowId) {
        try {
            UUID escrowPublicId = UUID.fromString(escrowId);
            EscrowResponse escrow = escrowService.getEscrowDetails(escrowPublicId);

            return ResponseEntity.ok(ApiResponse.success("Escrow detayları başarıyla getirildi", escrow));
        } catch (Exception e) {
            log.error("Escrow detayları getirilirken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Escrow detayları getirilemedi: " + e.getMessage()));
        }
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<EscrowResponse>> createEscrowForAuction(
            @RequestParam String auctionId,
            @RequestParam String winningBidId,
            @RequestParam(defaultValue = "0.05") BigDecimal commissionRate) {
        try {
            UUID auctionPublicId = UUID.fromString(auctionId);
            UUID winningBidPublicId = UUID.fromString(winningBidId);
            
            EscrowResponse escrow = escrowService.createEscrowForAuction(
                    auctionPublicId, winningBidPublicId, commissionRate);

            return ResponseEntity.ok(ApiResponse.success("Escrow başarıyla oluşturuldu ve para tutuldu", escrow));
        } catch (Exception e) {
            log.error("Escrow oluşturulurken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Escrow oluşturulamadı: " + e.getMessage()));
        }
    }

    @PutMapping("/{escrowId}/release")
    public ResponseEntity<ApiResponse<EscrowResponse>> releaseEscrow(
            @PathVariable String escrowId,
            @RequestParam(required = false) String notes) {
        try {
            UUID escrowPublicId = UUID.fromString(escrowId);
            EscrowResponse escrow = escrowService.releaseEscrow(escrowPublicId, notes);

            return ResponseEntity.ok(ApiResponse.success("Escrow başarıyla serbest bırakıldı, satıcıya ödeme yapıldı", escrow));
        } catch (Exception e) {
            log.error("Escrow serbest bırakılırken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Escrow serbest bırakılamadı: " + e.getMessage()));
        }
    }

    @PutMapping("/{escrowId}/refund")
    public ResponseEntity<ApiResponse<EscrowResponse>> refundEscrow(
            @PathVariable String escrowId,
            @RequestParam String reason) {
        try {
            UUID escrowPublicId = UUID.fromString(escrowId);
            EscrowResponse escrow = escrowService.refundEscrow(escrowPublicId, reason);

            return ResponseEntity.ok(ApiResponse.success("Escrow başarıyla iade edildi, alıcıya para geri verildi", escrow));
        } catch (Exception e) {
            log.error("Escrow iade edilirken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Escrow iade edilemedi: " + e.getMessage()));
        }
    }

    private UUID extractUserPublicIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor("9zcXacDJZLq9w7ifeZN7sCBKwfiTmpzzypHXTI6EkpFCPrd9daBy1eWCGRmiWWUHn3Ec/YFhgppxA7s9WtKb7w==".getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            String userPublicIdStr = claims.get("id", String.class);
            return UUID.fromString(userPublicIdStr);
        }
        throw new IllegalArgumentException("Geçersiz token");
    }
}