package com.tendering.controller;

import com.tendering.dto.common.ApiResponse;
import com.tendering.dto.request.payment.BankAccountCreateRequest;
import com.tendering.dto.response.payment.BankAccountResponse;
import com.tendering.service.BankAccountService;
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
@RequestMapping("/api/payment/bank-accounts")
@RequiredArgsConstructor
@Slf4j
public class BankAccountController {

    private final BankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<ApiResponse<BankAccountResponse>> createBankAccount(
            @Valid @RequestBody BankAccountCreateRequest request,
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            BankAccountResponse response = bankAccountService.createBankAccount(userPublicId, request);

            ApiResponse<BankAccountResponse> apiResponse = ApiResponse.success(
                    "Banka hesabı başarıyla oluşturuldu", response);

            return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
        } catch (Exception e) {
            log.error("Banka hesabı oluşturulurken hata oluştu", e);
            ApiResponse<BankAccountResponse> errorResponse = ApiResponse.error(
                    "Banka hesabı oluşturulamadı: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BankAccountResponse>>> getUserBankAccounts(
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            List<BankAccountResponse> accounts = bankAccountService.getUserBankAccounts(userPublicId);

            ApiResponse<List<BankAccountResponse>> apiResponse = ApiResponse.success(
                    "Banka hesapları başarıyla getirildi", accounts);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Banka hesapları getirilirken hata oluştu", e);
            ApiResponse<List<BankAccountResponse>> errorResponse = ApiResponse.error(
                    "Banka hesapları getirilemedi: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<ApiResponse<BankAccountResponse>> getBankAccountDetails(
            @PathVariable String accountId,
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            UUID accountPublicId = UUID.fromString(accountId);
            BankAccountResponse account = bankAccountService.getBankAccountDetails(userPublicId, accountPublicId);

            ApiResponse<BankAccountResponse> apiResponse = ApiResponse.success(
                    "Banka hesabı detayları başarıyla getirildi", account);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Banka hesabı detayları getirilirken hata oluştu", e);
            ApiResponse<BankAccountResponse> errorResponse = ApiResponse.error(
                    "Banka hesabı detayları getirilemedi: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PutMapping("/{accountId}/set-default")
    public ResponseEntity<ApiResponse<BankAccountResponse>> setDefaultBankAccount(
            @PathVariable String accountId,
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            UUID accountPublicId = UUID.fromString(accountId);
            BankAccountResponse response = bankAccountService.setDefaultBankAccount(userPublicId, accountPublicId);

            ApiResponse<BankAccountResponse> apiResponse = ApiResponse.success(
                    "Varsayılan banka hesabı başarıyla güncellendi", response);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Varsayılan banka hesabı güncellenirken hata oluştu", e);
            ApiResponse<BankAccountResponse> errorResponse = ApiResponse.error(
                    "Varsayılan banka hesabı güncellenemedi: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> deleteBankAccount(
            @PathVariable String accountId,
            HttpServletRequest httpRequest) {
        try {
            UUID userPublicId = extractUserPublicIdFromRequest(httpRequest);
            UUID accountPublicId = UUID.fromString(accountId);
            bankAccountService.deleteBankAccount(userPublicId, accountPublicId);

            ApiResponse<Void> apiResponse = ApiResponse.success(
                    "Banka hesabı başarıyla silindi", null);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Banka hesabı silinirken hata oluştu", e);
            ApiResponse<Void> errorResponse = ApiResponse.error(
                    "Banka hesabı silinemedi: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // Admin endpoint - Banka hesabını doğrula
    @PutMapping("/{accountId}/verify")
    public ResponseEntity<ApiResponse<BankAccountResponse>> verifyBankAccount(
            @PathVariable String accountId) {
        try {
            UUID accountPublicId = UUID.fromString(accountId);
            BankAccountResponse response = bankAccountService.verifyBankAccount(accountPublicId);

            ApiResponse<BankAccountResponse> apiResponse = ApiResponse.success(
                    "Banka hesabı başarıyla doğrulandı", response);

            return ResponseEntity.ok(apiResponse);
        } catch (Exception e) {
            log.error("Banka hesabı doğrulanırken hata oluştu", e);
            ApiResponse<BankAccountResponse> errorResponse = ApiResponse.error(
                    "Banka hesabı doğrulanamadı: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
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