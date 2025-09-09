package com.tendering.controller;

import com.tendering.dto.common.ApiResponse;
import com.tendering.dto.request.payment.CreditCardCreateRequest;
import com.tendering.dto.response.payment.CreditCardResponse;
import com.tendering.service.CreditCardService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment/credit-cards")
@RequiredArgsConstructor
@Slf4j
public class CreditCardController {

    private final CreditCardService creditCardService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreditCardResponse>> createCreditCard(
            @Valid @RequestBody CreditCardCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            CreditCardResponse response = creditCardService.createCreditCard(userPublicId, request);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Kredi kartı başarıyla eklendi", response));
        } catch (Exception e) {
            log.error("Kredi kartı eklenirken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Kredi kartı eklenemedi: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CreditCardResponse>>> getUserCreditCards(
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            List<CreditCardResponse> cards = creditCardService.getUserCreditCards(userPublicId);

            return ResponseEntity.ok(ApiResponse.success("Kredi kartları başarıyla getirildi", cards));
        } catch (Exception e) {
            log.error("Kredi kartları getirilirken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Kredi kartları getirilemedi: " + e.getMessage()));
        }
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<ApiResponse<CreditCardResponse>> getCreditCardDetails(
            @PathVariable String cardId,
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            UUID cardPublicId = UUID.fromString(cardId);
            CreditCardResponse card = creditCardService.getCreditCardDetails(userPublicId, cardPublicId);

            return ResponseEntity.ok(ApiResponse.success("Kredi kartı detayları başarıyla getirildi", card));
        } catch (Exception e) {
            log.error("Kredi kartı detayları getirilirken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Kredi kartı detayları getirilemedi: " + e.getMessage()));
        }
    }

    @PutMapping("/{cardId}/set-default")
    public ResponseEntity<ApiResponse<CreditCardResponse>> setDefaultCreditCard(
            @PathVariable String cardId,
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            UUID cardPublicId = UUID.fromString(cardId);
            CreditCardResponse response = creditCardService.setDefaultCreditCard(userPublicId, cardPublicId);

            return ResponseEntity.ok(ApiResponse.success("Varsayılan kredi kartı başarıyla güncellendi", response));
        } catch (Exception e) {
            log.error("Varsayılan kredi kartı güncellenirken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Varsayılan kredi kartı güncellenemedi: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{cardId}")
    public ResponseEntity<ApiResponse<Void>> deleteCreditCard(
            @PathVariable String cardId,
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            UUID cardPublicId = UUID.fromString(cardId);
            creditCardService.deleteCreditCard(userPublicId, cardPublicId);

            return ResponseEntity.ok(ApiResponse.success("Kredi kartı başarıyla silindi", null));
        } catch (Exception e) {
            log.error("Kredi kartı silinirken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Kredi kartı silinemedi: " + e.getMessage()));
        }
    }

    @PutMapping("/{cardId}/verify")
    public ResponseEntity<ApiResponse<CreditCardResponse>> verifyCreditCard(
            @PathVariable String cardId,
            @RequestParam String gatewayToken) {
        try {
            UUID cardPublicId = UUID.fromString(cardId);
            CreditCardResponse response = creditCardService.verifyCreditCard(cardPublicId, gatewayToken);

            return ResponseEntity.ok(ApiResponse.success("Kredi kartı başarıyla doğrulandı", response));
        } catch (Exception e) {
            log.error("Kredi kartı doğrulanırken hata oluştu", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Kredi kartı doğrulanamadı: " + e.getMessage()));
        }
    }

    private UUID extractUserPublicIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            Claims claims = Jwts.parser()
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